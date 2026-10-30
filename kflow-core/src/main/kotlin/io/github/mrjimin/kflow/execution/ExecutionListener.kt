package io.github.mrjimin.kflow.execution

fun interface ExecutionListener {
    suspend fun onEvent(event: WorkflowEvent)
}