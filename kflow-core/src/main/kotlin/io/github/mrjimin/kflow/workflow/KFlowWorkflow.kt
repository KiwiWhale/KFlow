package io.github.mrjimin.kflow.workflow

import io.github.mrjimin.kflow.exception.WorkflowExecutionException
import io.github.mrjimin.kflow.execution.ExecutionListener
import io.github.mrjimin.kflow.execution.ExecutionResult
import io.github.mrjimin.kflow.execution.ExecutionStatus
import io.github.mrjimin.kflow.execution.ExecutionTracker
import kotlinx.coroutines.CancellationException
import java.util.*
import kotlin.time.TimeSource

class KFlowWorkflow<I, O> internal constructor(
    val name: String,
    private val action: suspend KFlowWorkflowScope<I>.() -> O,
    listeners: List<ExecutionListener> = emptyList()
) {

    private val listeners = listeners.toList()

    init {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank"
        }
    }

    suspend fun execute(input: I): O =
        executeWithResult(input).getOrThrow()

    suspend fun executeWithResult(input: I): ExecutionResult<O> {
        val executionId = UUID.randomUUID().toString()
        val startedAt = TimeSource.Monotonic.markNow()

        val tracker = ExecutionTracker(
            executionId = executionId,
            workflowName = name,
            listeners = listeners
        )

        val scope = KFlowWorkflowScope(
            input = input,
            tracker = tracker
        )

        return try {
            tracker.workflowStarted()

            val value = action(scope)
            val duration = startedAt.elapsedNow()

            tracker.workflowCompleted(duration)

            createResult(
                executionId = executionId,
                status = ExecutionStatus.SUCCESS,
                value = value,
                duration = duration,
                tracker = tracker
            )

        } catch (exception: CancellationException) {
            throw exception

        } catch (exception: Exception) {
            val duration = startedAt.elapsedNow()

            val error = WorkflowExecutionException(
                executionId = executionId,
                workflowName = name,
                cause = exception
            )

            tracker.workflowFailed(
                duration = duration,
                error = error
            )

            createResult(
                executionId = executionId,
                status = ExecutionStatus.FAILED,
                value = null,
                duration = duration,
                tracker = tracker,
                error = error
            )
        }
    }

    private suspend fun createResult(
        executionId: String,
        status: ExecutionStatus,
        value: O?,
        duration: kotlin.time.Duration,
        tracker: ExecutionTracker,
        error: WorkflowExecutionException? = null
    ): ExecutionResult<O> {
        return ExecutionResult(
            executionId = executionId,
            workflowName = name,
            status = status,
            value = value,
            steps = tracker.snapshot(),
            duration = duration,
            error = error
        )
    }
}
