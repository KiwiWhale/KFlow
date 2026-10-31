package io.github.mrjimin.kflow.ktor.registry

import io.github.mrjimin.kflow.core.workflow.KFlowWorkflow
import java.util.concurrent.ConcurrentHashMap

class WorkflowRegistry {

    private val workflows =
        ConcurrentHashMap<String, KFlowWorkflow<*, *>>()

    val size: Int
        get() = workflows.size

    fun register(
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

    fun get(name: String): KFlowWorkflow<*, *> =
        workflows[name]
            ?: throw NoSuchElementException(
                "Workflow '$name' is not registered."
            )

    fun getOrNull(name: String): KFlowWorkflow<*, *>? =
        workflows[name]

    fun contains(name: String): Boolean =
        workflows.containsKey(name)

    fun names(): Set<String> =
        workflows.keys.toSet()

    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(name: String): KFlowWorkflow<I, O> =
        get(name) as KFlowWorkflow<I, O>
}