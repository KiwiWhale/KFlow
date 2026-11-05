package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

internal val KiwiFlowApplicationKey =
    AttributeKey<KiwiFlowApplication>("KiwiFlowApplication")

/**
 * Read-only facade over the Core workflow registry.
 */
class KiwiFlowApplication internal constructor(
    private val registry: KiwiWorkflowRegistry
) {

    val size: Int
        get() = registry.size

    val isEmpty: Boolean
        get() = registry.isEmpty

    fun keys(): Set<String> =
        registry.keys()

    operator fun contains(
        key: KiwiWorkflowKey<*, *>
    ): Boolean =
        key in registry

    operator fun contains(name: String): Boolean =
        name in registry

    operator fun get(
        key: KiwiWorkflowKey<*, *>
    ): KiwiWorkflow<*, *> =
        registry[key]

    operator fun get(name: String): KiwiWorkflow<*, *>? =
        registry[name]

    fun getOrNull(
        key: KiwiWorkflowKey<*, *>
    ): KiwiWorkflow<*, *>? =
        registry.getOrNull(key)

    fun getOrNull(name: String): KiwiWorkflow<*, *>? =
        registry.getOrNull(name)

    fun <I, O> workflow(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> =
        registry[key]

    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(name: String): KiwiWorkflow<I, O> =
        registry.workflow(name)
}

/**
 * Accessor for the KiwiFlow application facade.
 */
val Application.kiwiFlow: KiwiFlowApplication
    get() = attributes[KiwiFlowApplicationKey]