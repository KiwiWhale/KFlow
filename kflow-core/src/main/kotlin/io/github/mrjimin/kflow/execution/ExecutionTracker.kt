package io.github.mrjimin.kflow.execution

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

internal class ExecutionTracker(
    private val executionId: String,
    private val workflowName: String,
    listeners: List<ExecutionListener> = emptyList()
) {

    private val listeners = listeners.toList()

    private val records = mutableListOf<StepExecution>()

    private val recordsMutex = Mutex()
    private val eventMutex = Mutex()

    suspend fun workflowStarted() {
        emit(WorkflowEventType.WORKFLOW_STARTED)
    }

    suspend fun workflowCompleted(duration: Duration) {
        emit(
            type = WorkflowEventType.WORKFLOW_COMPLETED,
            duration = duration
        )
    }

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

    suspend fun stepStarted(
        name: String,
        attempt: Int
    ) {
        emit(
            type = WorkflowEventType.STEP_STARTED,
            stepName = name,
            attempt = attempt
        )
    }

    suspend fun stepRetrying(
        name: String,
        attempt: Int,
        retryDelay: Duration,
        error: Throwable
    ) {
        emit(
            type = WorkflowEventType.STEP_RETRYING,
            stepName = name,
            attempt = attempt,
            duration = retryDelay,
            error = error
        )
    }

    suspend fun record(
        name: String,
        status: ExecutionStatus,
        duration: Duration,
        attempt: Int = 1,
        error: Throwable? = null
    ) {
        recordsMutex.withLock {
            records.add(
                StepExecution(
                    name = name,
                    status = status,
                    duration = duration,
                    attempt = attempt,
                    error = error
                )
            )
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

        for (listener in listeners) {
            try {
                listener.onEvent(event)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // Listener failures must not interrupt workflow execution.
            }
        }
    }

    suspend fun snapshot(): List<StepExecution> =
        recordsMutex.withLock {
            records.toList()
        }
}