package io.github.kiwiwhale.kiwiflow.core.step

import io.github.kiwiwhale.kiwiflow.core.exception.RetryExhaustedException
import io.github.kiwiwhale.kiwiflow.core.exception.StepException
import io.github.kiwiwhale.kiwiflow.core.exception.StepTimeoutException
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionStatus
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionTracker
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowScope
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Internal executor responsible for running a configured workflow step.
 *
 * Handles step invocation, timeout enforcement, retry decisions,
 * execution tracking, and final failure propagation.
 *
 * @param I Workflow input type.
 * @param O Step output type.
 * @property name Step name.
 * @property action Step execution logic.
 * @property policy Execution policies applied to this step.
 */
internal class Step<I, O>(
    private val name: String,
    private val action: suspend KiwiWorkflowScope<I>.() -> O,
    private val policy: StepPolicy
) {

    /**
     * Executes the step using its configured policies.
     *
     * Each attempt is recorded by the execution tracker.
     * Eligible failures are retried according to the configured policy.
     *
     * Coroutine cancellation is propagated without being treated
     * as an ordinary retryable failure.
     *
     * @param scope Workflow execution scope.
     * @param tracker Execution tracker.
     * @return Result produced by the successful attempt.
     * @throws StepException If the step fails and no retry is performed.
     * @throws RetryExhaustedException If all retry attempts are exhausted.
     * @throws StepTimeoutException If the step exceeds its timeout.
     * @throws CancellationException If execution is cancelled.
     */
    suspend fun execute(
        scope: KiwiWorkflowScope<I>,
        tracker: ExecutionTracker
    ): O {
        val retry = policy.retry
        val maxAttempts = retry?.maxAttempts ?: 1

        var attempt = 1

        while (true) {
            val startedAt = TimeSource.Monotonic.markNow()

            try {
                tracker.stepStarted(name, attempt)

                val result = executeAction(scope)

                tracker.record(
                    name = name,
                    status = ExecutionStatus.SUCCESS,
                    duration = startedAt.elapsedNow(),
                    attempt = attempt
                )

                return result

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                tracker.record(
                    name = name,
                    status = ExecutionStatus.FAILED,
                    duration = startedAt.elapsedNow(),
                    attempt = attempt,
                    error = exception
                )

                val shouldRetry = retry?.shouldRetry(exception) == true

                if (!shouldRetry) {
                    throw StepException(
                        stepName = name,
                        attempt = attempt,
                        message = exception.message ?: "Unknown error",
                        cause = exception
                    )
                }

                if (attempt >= maxAttempts) {
                    throw RetryExhaustedException(
                        stepName = name,
                        maxAttempts = maxAttempts,
                        cause = exception
                    )
                }

                val retryDelay = retry.delayFor(attempt)

                tracker.stepRetrying(
                    name = name,
                    attempt = attempt,
                    retryDelay = retryDelay,
                    error = exception
                )

                delay(retryDelay)
                attempt++
            }
        }
    }

    private suspend fun executeAction(
        scope: KiwiWorkflowScope<I>
    ): O {
        val timeout = policy.timeout
            ?: return action(scope)

        val result = withTimeoutOrNull(timeout) {
            ActionResult(action(scope))
        } ?: throw StepTimeoutException(name, timeout)

        return result.value
    }

    private data class ActionResult<T>(val value: T)
}