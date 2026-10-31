package io.github.mrjimin.kflow

import io.github.mrjimin.kflow.execution.ExecutionListener
import io.github.mrjimin.kflow.workflow.KFlowWorkflow
import io.github.mrjimin.kflow.workflow.KFlowWorkflowScope

object KFlow {

    fun <I, O> workflow(
        name: String,
        listeners: List<ExecutionListener> = emptyList(),
        block: suspend KFlowWorkflowScope<I>.() -> O
    ): KFlowWorkflow<I, O> {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank"
        }

        return KFlowWorkflow(
            name = name,
            action = block,
            listeners = listeners.toList()
        )
    }
}