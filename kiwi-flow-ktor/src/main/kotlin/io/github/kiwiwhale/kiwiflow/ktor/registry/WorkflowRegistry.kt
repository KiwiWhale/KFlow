package io.github.kiwiwhale.kiwiflow.ktor.registry

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import java.util.concurrent.ConcurrentHashMap

class WorkflowRegistry {

    private val workflows =
        ConcurrentHashMap<String, KiwiFlowWorkflow<*, *>>()

    val size: Int
        get() = workflows.size

    fun register(
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

    fun get(name: String): KiwiFlowWorkflow<*, *> =
        workflows[name]
            ?: throw NoSuchElementException("Workflow '$name' is not registered.")

    fun getOrNull(name: String): KiwiFlowWorkflow<*, *>? =
        workflows[name]

    fun contains(name: String): Boolean =
        workflows.containsKey(name)

    fun names(): Set<String> =
        workflows.keys.toSet()

    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(name: String): KiwiFlowWorkflow<I, O> =
        get(name) as KiwiFlowWorkflow<I, O>
}