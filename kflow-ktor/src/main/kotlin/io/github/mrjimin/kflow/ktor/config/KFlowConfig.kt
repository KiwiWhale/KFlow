package io.github.mrjimin.kflow.ktor.config

import io.github.mrjimin.kflow.core.workflow.KFlowWorkflow

class KFlowConfig {

    private val workflows =
        linkedMapOf<String, KFlowWorkflow<*, *>>()

    fun workflow(
        name: String,
        workflow: KFlowWorkflow<*, *>
    ) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }

        check(workflows.putIfAbsent(name, workflow) == null) {
            "Workflow '$name' is already registered."
        }
    }

    fun workflows(
        workflows: Map<String, KFlowWorkflow<*, *>>
    ) {
        workflows.forEach { (name, workflow) ->
            workflow(name, workflow)
        }
    }

    internal fun registeredWorkflows():
            Map<String, KFlowWorkflow<*, *>> =
        workflows.toMap()
}