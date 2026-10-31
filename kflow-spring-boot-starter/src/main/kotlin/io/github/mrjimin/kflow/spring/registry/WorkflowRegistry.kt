package io.github.mrjimin.kflow.spring.registry

import io.github.mrjimin.kflow.workflow.KFlowWorkflow
import java.util.concurrent.ConcurrentHashMap

class WorkflowRegistry {

    private val workflows =
        ConcurrentHashMap<String, KFlowWorkflow<*, *>>()

    fun register(
        name: String,
        workflow: KFlowWorkflow<*, *>
    ) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }

        require(workflows.putIfAbsent(name, workflow) == null) {
            "Workflow '$name' is already registered."
        }
    }

    fun get(name: String): KFlowWorkflow<*, *> {
        return workflows[name]
            ?: throw NoSuchElementException("Workflow '$name' is not registered.")
    }

    fun contains(name: String): Boolean =
        workflows.containsKey(name)

    fun names(): Set<String> =
        workflows.keys.toSet()
}
