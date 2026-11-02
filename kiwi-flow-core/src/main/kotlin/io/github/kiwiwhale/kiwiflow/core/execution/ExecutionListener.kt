package io.github.kiwiwhale.kiwiflow.core.execution

fun interface ExecutionListener {
    suspend fun onEvent(event: WorkflowEvent)
}