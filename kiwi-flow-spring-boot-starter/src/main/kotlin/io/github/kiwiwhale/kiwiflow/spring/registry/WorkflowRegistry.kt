package io.github.kiwiwhale.kiwiflow.spring.registry

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import java.util.concurrent.ConcurrentHashMap

class WorkflowRegistry {

    private val workflows =
        ConcurrentHashMap<String, KiwiFlowWorkflow<*, *>>()

    fun register(
        name: String,
        workflow: KiwiFlowWorkflow<*, *>
    ) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }

        require(workflows.putIfAbsent(name, workflow) == null) {
            "Workflow '$name' is already registered."
        }
    }

    fun get(name: String): KiwiFlowWorkflow<*, *> {
        return workflows[name]
            ?: throw NoSuchElementException("Workflow '$name' is not registered.")
    }

    fun getOrNull(name: String): KiwiFlowWorkflow<*, *>? =
        workflows[name]

    fun contains(name: String): Boolean =
        workflows.containsKey(name)

    fun names(): Set<String> =
        workflows.keys.toSet()
}
