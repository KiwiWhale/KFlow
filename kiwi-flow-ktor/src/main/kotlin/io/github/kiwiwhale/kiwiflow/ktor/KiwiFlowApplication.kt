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

    fun names(): Set<String> =
        registry.names()

    fun keys(): Set<KiwiWorkflowKey<*, *>> =
        registry.keys()

    fun contains(key: KiwiWorkflowKey<*, *>): Boolean =
        registry.contains(key)

    fun contains(name: String): Boolean =
        registry.contains(name)

    fun get(key: KiwiWorkflowKey<*, *>): KiwiWorkflow<*, *> =
        registry.get(key.name)

    fun get(name: String): KiwiWorkflow<*, *> =
        registry.get(name)

    fun <I, O> workflow(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> =
        registry.workflow(key)

    fun <I, O> workflow(name: String): KiwiWorkflow<I, O> =
        registry.workflow(name)
}

val Application.KiwiFlow: KiwiFlowApplication
    get() = attributes[KiwiFlowApplicationKey]
