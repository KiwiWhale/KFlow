package io.github.kiwiwhale.kiwiflow.core.execution

import kotlin.time.Duration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Tracks workflow and step execution events and records.
 *
 * Responsible for notifying registered listeners and maintaining
 * an ordered collection of step execution attempts.
 *
 * Listener callbacks are executed sequentially. Non-cancellation
 * listener exceptions are isolated from workflow execution.
 *
 * @property executionId Unique identifier of the execution.
 * @property workflowName Name of the workflow being executed.
 * @property listeners Registered execution listeners.
 */
internal class ExecutionTracker(
    private val executionId: String,
    private val workflowName: String,
    listeners: List<ExecutionListener> = emptyList()
) {

    private val listeners = listeners.toList()

    private val records = mutableListOf<StepExecution>()
    private val recordsMutex = Mutex()

    /**
     * Emits the workflow started event.
     */
    suspend fun workflowStarted() {
        emit(WorkflowEventType.WORKFLOW_STARTED)
    }

    /**
     * Emits the workflow completed event.
     *
     * @param duration Total workflow execution duration.
     */
    suspend fun workflowCompleted(duration: Duration) {
        emit(
            type = WorkflowEventType.WORKFLOW_COMPLETED,
            duration = duration
        )
    }

    /**
     * Emits the workflow failed event.
     *
     * @param duration Total workflow execution duration.
     * @param error Failure associated with the workflow.
     */
    suspend fun workflowFailed(
        duration: Duration,
        error: Throwable
    ) {
        emit(
            type = WorkflowEventType.WORKFLOW_FAILED,
            duration = duration,
            error = error
        )
    }

    /**
     * Emits a step started event.
     *
     * @param name Step name.
     * @param attempt Current execution attempt, starting at one.
     */
    suspend fun stepStarted(
        name: String,
        attempt: Int
    ) {
        require(attempt >= 1) {
            "attempt must be at least 1"
        }

        emit(
            type = WorkflowEventType.STEP_STARTED,
            stepName = name,
            attempt = attempt
        )
    }

    /**
     * Emits an event indicating that a failed step will be retried.
     *
     * @param name Step name.
     * @param attempt Failed execution attempt.
     * @param retryDelay Delay before the next attempt.
     * @param error Failure that triggered the retry.
     */
    suspend fun stepRetrying(
        name: String,
        attempt: Int,
        retryDelay: Duration,
        error: Throwable
    ) {
        require(attempt >= 1) {
            "attempt must be at least 1"
        }

        emit(
            type = WorkflowEventType.STEP_RETRYING,
            stepName = name,
            attempt = attempt,
            retryDelay = retryDelay,
            error = error
        )
    }

    /**
     * Records a step execution attempt and emits its completion event.
     *
     * @param name Step name.
     * @param status Execution outcome.
     * @param duration Execution duration.
     * @param attempt Execution attempt number.
     * @param error Failure associated with the attempt, if any.
     */
    suspend fun record(
        name: String,
        status: ExecutionStatus,
        duration: Duration,
        attempt: Int = 1,
        error: Throwable? = null
    ) {
        require(attempt >= 1) {
            "attempt must be at least 1"
        }

        val execution = StepExecution(
            name = name,
            status = status,
            duration = duration,
            attempt = attempt,
            error = error
        )

        recordsMutex.withLock {
            records.add(execution)
        }

        emit(
            type = when (status) {
                ExecutionStatus.SUCCESS ->
                    WorkflowEventType.STEP_COMPLETED

                ExecutionStatus.FAILED ->
                    WorkflowEventType.STEP_FAILED
            },
            stepName = name,
            attempt = attempt,
            duration = duration,
            error = error
        )
    }

    private suspend fun emit(
        type: WorkflowEventType,
        stepName: String? = null,
        attempt: Int? = null,
        duration: Duration? = null,
        retryDelay: Duration? = null,
        error: Throwable? = null
    ) {
        val event = WorkflowEvent(
            executionId = executionId,
            workflowName = workflowName,
            type = type,
            stepName = stepName,
            attempt = attempt,
            duration = duration,
            retryDelay = retryDelay,
            error = error
        )

        val coroutineContext = currentCoroutineContext()

        for (listener in listeners) {
            coroutineContext.ensureActive()

            try {
                listener.onEvent(event)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // Listener failures must not interrupt workflow execution.
            }
        }
    }

    /**
     * Returns an immutable snapshot of recorded step executions.
     *
     * @return Step execution records in insertion order.
     */
    suspend fun snapshot(): List<StepExecution> =
        recordsMutex.withLock {
            records.toList()
        }
}
