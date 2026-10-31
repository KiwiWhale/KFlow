package io.github.mrjimin.kflow.spring.registry

import io.github.mrjimin.kflow.workflow.Workflow
import java.util.concurrent.ConcurrentHashMap

class WorkflowRegistry {

    private val workflows =
        ConcurrentHashMap<String, Workflow<*, *>>()

    fun register(
        name: String,
        workflow: Workflow<*, *>
    ) {
        require(name.isNotBlank()) {
            "Workflow name must not be blank."
        }

        require(workflows.putIfAbsent(name, workflow) == null) {
            "Workflow '$name' is already registered."
        }
    }

    fun get(name: String): Workflow<*, *> {
        return workflows[name]
            ?: throw NoSuchElementException("Workflow '$name' is not registered.")
    }

    fun contains(name: String): Boolean {
        return workflows.containsKey(name)
    }

    fun names(): Set<String> {
        return workflows.keys.toSet()
    }
}