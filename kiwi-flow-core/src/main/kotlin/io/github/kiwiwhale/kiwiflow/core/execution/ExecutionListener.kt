package io.github.kiwiwhale.kiwiflow.core.execution

/**
 * Receives lifecycle events emitted during workflow execution.
 *
 * Implementations can observe workflow and step execution without
 * participating directly in the execution process.
 *
 * Listener callbacks are invoked sequentially.
 *
 * Listener exceptions do not fail workflow execution, while
 * coroutine cancellation is propagated.
 *
 * @see WorkflowEvent
 * @see WorkflowEventType
 */
fun interface ExecutionListener {

    /**
     * Handles a workflow execution event.
     *
     * @param event Event describing the current execution state.
     */
    suspend fun onEvent(event: WorkflowEvent)
}