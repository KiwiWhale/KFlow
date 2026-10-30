package io.github.mrjimin.kflow.execution

import kotlinx.coroutines.CancellationException
import kotlin.time.Duration

internal class ExecutionTracker(
    private val executionId: String,
    private val workflowName: String,
    private val listeners: List<ExecutionListener> = emptyList()
) {

    private val records = mutableListOf<StepExecution>()

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

    suspend fun stepStarted(name: String) {
        emit(
            type = WorkflowEventType.STEP_STARTED,
            stepName = name
        )
    }

    suspend fun record(
        name: String,
        status: ExecutionStatus,
        duration: Duration,
        attempt: Int = 1,
        error: Throwable? = null
    ) {
        records.add(
            StepExecution(
                name = name,
                status = status,
                duration = duration,
                attempt = attempt,
                error = error
            )
        )

        emit(
            type = when (status) {
                ExecutionStatus.SUCCESS ->
                    WorkflowEventType.STEP_COMPLETED

                ExecutionStatus.FAILED ->
                    WorkflowEventType.STEP_FAILED
            },
            stepName = name,
            duration = duration,
            error = error
        )
    }

    suspend fun emit(
        type: WorkflowEventType,
        stepName: String? = null,
        duration: Duration? = null,
        error: Throwable? = null
    ) {
        val event = WorkflowEvent(
            executionId = executionId,
            workflowName = workflowName,
            type = type,
            stepName = stepName,
            duration = duration,
            error = error
        )

        listeners.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
            }
        }
    }

    fun snapshot(): List<StepExecution> =
        records.toList()
}
