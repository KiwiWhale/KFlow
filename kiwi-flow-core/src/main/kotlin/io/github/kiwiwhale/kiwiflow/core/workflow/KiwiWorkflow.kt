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
 * A workflow encapsulates its execution logic and registered
 * execution listeners.
 *
 * Each invocation creates an independent execution context
 * and receives a unique execution identifier.
 *
 * Workflow executions produce an [ExecutionResult] containing
 * the final status, output, duration, and step execution records.
 *
 * The workflow is identified by its [key].
 *
 * @param I Workflow input type.
 * @param O Workflow output type.
 * @property key Typed workflow identifier.
 * @property name Name derived from [key].
 *
 * @see KiwiWorkflowKey
 * @see KiwiWorkflowScope
 * @see ExecutionResult
 */
class KiwiWorkflow<I, O> internal constructor(
    val key: KiwiWorkflowKey<I, O>,
    private val action: suspend KiwiWorkflowScope<I>.() -> O,
    listeners: List<ExecutionListener> = emptyList()
) {

    private val listeners = listeners.toList()

    /**
     * Name of this workflow.
     *
     * The name is derived directly from [key] to ensure
     * that workflow identification has a single source of truth.
     */
    val name: String
        get() = key.name

    /**
     * Executes the workflow and returns its output.
     *
     * Unlike [executeWithResult], this method throws the
     * workflow execution exception when execution fails.
     *
     * Coroutine cancellation is propagated without wrapping.
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
     * Execution failures are represented by an [ExecutionResult]
     * with [ExecutionStatus.FAILED] instead of being thrown directly.
     *
     * Coroutine cancellation is still propagated.
     *
     * @param input Input supplied to the workflow.
     * @return Detailed workflow execution result.
     * @throws CancellationException If execution is cancelled.
     */
    suspend fun executeWithResult(
        input: I
    ): ExecutionResult<O> {
        val executionId = UUID.randomUUID().toString()
        val startedAt = TimeSource.Monotonic.markNow()

        val tracker = ExecutionTracker(
            executionId = executionId,
            workflowName = name,
            listeners = listeners
        )

        val scope = KiwiWorkflowScope(
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

    /**
     * Converts a workflow execution failure into an execution result.
     *
     * Existing [WorkflowExecutionException] instances are preserved.
     * Other exceptions are wrapped with the current execution identifier.
     *
     * @param exception Exception raised during execution.
     * @param executionId Identifier of the current execution.
     * @param startedAt Monotonic execution start time.
     * @param tracker Execution event and step tracker.
     * @return Failed workflow execution result.
     */
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

    /**
     * Creates an immutable snapshot of the workflow execution result.
     *
     * @param executionId Current execution identifier.
     * @param status Final execution status.
     * @param value Workflow output, or null on failure.
     * @param duration Total execution duration.
     * @param tracker Execution tracker.
     * @param error Workflow execution error, if any.
     * @return Workflow execution result.
     */
    private suspend fun createResult(
        executionId: String,
        status: ExecutionStatus,
        value: O?,
        duration: Duration,
        tracker: ExecutionTracker,
        error: WorkflowExecutionException? = null
    ): ExecutionResult<O> =
        ExecutionResult(
            executionId = executionId,
            workflowName = name,
            status = status,
            value = value,
            steps = tracker.snapshot(),
            duration = duration,
            error = error
        )
}
