package io.github.mrjimin.kflow.core.step

import io.github.mrjimin.kflow.core.exception.RetryExhaustedException
import io.github.mrjimin.kflow.core.exception.StepException
import io.github.mrjimin.kflow.core.exception.StepTimeoutException
import io.github.mrjimin.kflow.core.execution.ExecutionStatus
import io.github.mrjimin.kflow.core.execution.ExecutionTracker
import io.github.mrjimin.kflow.core.workflow.KFlowWorkflowScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.time.TimeSource

internal class Step<I, O>(
    private val name: String,
    private val action: suspend KFlowWorkflowScope<I>.() -> O,
    private val policy: StepPolicy
) {

    suspend fun execute(
        scope: KFlowWorkflowScope<I>,
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
        scope: KFlowWorkflowScope<I>
    ): O {
        val timeout = policy.timeout
            ?: return action(scope)

        try {
            return withTimeout(timeout) {
                action(scope)
            }
        } catch (exception: TimeoutCancellationException) {
            throw StepTimeoutException(
                stepName = name,
                timeout = timeout,
                cause = exception
            )
        }
    }
}