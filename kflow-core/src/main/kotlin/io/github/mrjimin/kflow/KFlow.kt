package io.github.mrjimin.kflow

import io.github.mrjimin.kflow.execution.ExecutionListener
import io.github.mrjimin.kflow.workflow.Workflow
import io.github.mrjimin.kflow.workflow.WorkflowScope

object KFlow {

    fun <I, O> workflow(
        name: String,
        listeners: List<ExecutionListener> = emptyList(),
        block: suspend WorkflowScope<I>.() -> O
    ): Workflow<I, O> {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank"
        }

        return Workflow(
            name = name,
            action = block,
            listeners = listeners.toList()
        )
    }
}