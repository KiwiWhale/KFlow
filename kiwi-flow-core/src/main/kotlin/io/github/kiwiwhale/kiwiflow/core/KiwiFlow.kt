package io.github.kiwiwhale.kiwiflow.core

import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionListener
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflowScope

object KiwiFlow {

    fun <I, O> workflow(
        name: String,
        listeners: List<ExecutionListener> = emptyList(),
        block: suspend KiwiFlowWorkflowScope<I>.() -> O
    ): KiwiFlowWorkflow<I, O> {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank"
        }

        return KiwiFlowWorkflow(
            name = name,
            action = block,
            listeners = listeners.toList()
        )
    }
}