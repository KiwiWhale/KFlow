package io.github.kiwiwhale.kiwiflow.ktor.config

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow

class KiwiFlowConfig {

    private val workflows =
        linkedMapOf<String, KiwiFlowWorkflow<*, *>>()

    fun workflow(
        name: String,
        workflow: KiwiFlowWorkflow<*, *>
    ) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }

        check(workflows.putIfAbsent(name, workflow) == null) {
            "Workflow '$name' is already registered."
        }
    }

    fun workflows(
        workflows: Map<String, KiwiFlowWorkflow<*, *>>
    ) {
        workflows.forEach { (name, workflow) ->
            workflow(name, workflow)
        }
    }

    internal fun registeredWorkflows():
            Map<String, KiwiFlowWorkflow<*, *>> =
        workflows.toMap()
}