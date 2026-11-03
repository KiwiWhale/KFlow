package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiflow.core.exception.WorkflowExecutionException
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionListener
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionResult
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionStatus
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionTracker
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException

/**
 * Represents an executable KiwiFlow workflow.
 *
 * A workflow encapsulates its execution logic and registered listeners.
 * Each invocation creates an independent execution context and
 * receives a unique execution identifier.
 *
 * Workflow executions produce execution results containing
 * their final status, output, duration, and step records.
 *
 * @param I Workflow input type.
 * @param O Workflow output type.
 * @property name Workflow name.
 */
class KiwiFlowWorkflow<I, O> internal constructor(
    val name: String,
    private val action: suspend KiwiFlowWorkflowScope<I>.() -> O,
    listeners: List<ExecutionListener> = emptyList()
) {

    private val listeners = listeners.toList()

    init {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank"
        }
    }

    /**
     * Executes the workflow and returns its output.
     *
     * @param input Input supplied to the workflow.
     * @return Workflow output.
     * @throws WorkflowExecutionException If workflow execution fails.
     * @throws CancellationException If execution is cancelled.
     */
    suspend fun execute(input: I): O =
        executeWithResult(input).getOrThrow()

    /**
     * Executes the workflow and returns detailed execution information.
     *
     * Unlike [execute], workflow execution failures are represented
     * by an [ExecutionResult] rather than being thrown directly.
     *
     * Coroutine cancellation is still propagated.
     *
     * @param input Input supplied to the workflow.
     * @return Detailed workflow execution result.
     * @throws CancellationException If execution is cancelled.
     */
    suspend fun executeWithResult(input: I): ExecutionResult<O> {
        val executionId = UUID.randomUUID().toString()
        val startedAt = TimeSource.Monotonic.markNow()

        val tracker = ExecutionTracker(
            executionId = executionId,
            workflowName = name,
            listeners = listeners
        )

        val scope = KiwiFlowWorkflowScope(
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
            handleFailure(
                exception = exception,
                executionId = executionId,
                startedAt = startedAt,
                tracker = tracker
            )
        }
    }

    private suspend fun handleFailure(
        exception: Exception,
        executionId: String,
        startedAt: TimeSource.Monotonic.ValueTimeMark,
        tracker: ExecutionTracker
    ): ExecutionResult<O> {
        val duration = startedAt.elapsedNow()

        val error = exception as? WorkflowExecutionException
            ?: WorkflowExecutionException(
                executionId = executionId,
                workflowName = name,
                cause = exception
            )

        tracker.workflowFailed(
            duration = duration,
            error = error
        )

        return createResult(
            executionId = executionId,
            status = ExecutionStatus.FAILED,
            value = null,
            duration = duration,
            tracker = tracker,
            error = error
        )
    }

    private suspend fun createResult(
        executionId: String,
        status: ExecutionStatus,
        value: O?,
        duration: Duration,
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
