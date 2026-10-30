package io.github.mrjimin.kflow.step

import io.github.mrjimin.kflow.exception.RetryExhaustedException
import io.github.mrjimin.kflow.exception.StepException
import io.github.mrjimin.kflow.exception.StepTimeoutException
import io.github.mrjimin.kflow.execution.ExecutionStatus
import io.github.mrjimin.kflow.execution.ExecutionTracker
import io.github.mrjimin.kflow.workflow.WorkflowScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.TimeSource

internal class Step<I, O>(
    private val name: String,
    private val action: suspend WorkflowScope<I>.() -> O,
    private val policy: StepPolicy
) {

    suspend fun execute(
        scope: WorkflowScope<I>,
        tracker: ExecutionTracker
    ): O {
        val retry = policy.retry
        val maxAttempts = retry?.maxAttempts ?: 1

        for (attempt in 1..maxAttempts) {
            val startedAt = TimeSource.Monotonic.markNow()

            try {
                tracker.stepStarted(name)

                val result = if (policy.timeout != null) {
                    withTimeoutOrNull(policy.timeout) {
                        action(scope)
                    } ?: throw StepTimeoutException(name, policy.timeout)
                } else {
                    action(scope)
                }

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

                if (attempt == maxAttempts || retry?.shouldRetry(exception) != true) {
                    if (retry != null && attempt == maxAttempts) {
                        throw RetryExhaustedException(
                            stepName = name,
                            maxAttempts = maxAttempts,
                            cause = exception
                        )
                    }

                    throw StepException(
                        stepName = name,
                        attempt = attempt,
                        message = exception.message ?: "Unknown error",
                        cause = exception
                    )
                }

                delay(retry.delayFor(attempt))
            }
        }

        error("Unreachable step state: $name")
    }
}