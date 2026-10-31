package io.github.mrjimin.kflow.core.execution

fun interface ExecutionListener {
    suspend fun onEvent(event: WorkflowEvent)
}