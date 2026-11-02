package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import io.github.kiwiwhale.kiwiflow.ktor.registry.WorkflowRegistry
import io.ktor.server.application.*
import io.ktor.util.*

internal val KiwiFlowApplicationKey =
    AttributeKey<KiwiFlowApplication>("KiwiFlowApplication")

class KiwiFlowApplication internal constructor(
    val registry: WorkflowRegistry
) {

    val size: Int
        get() = registry.size

    fun names(): Set<String> =
        registry.names()

    fun contains(name: String): Boolean =
        registry.contains(name)

    fun get(name: String): KiwiFlowWorkflow<*, *> =
        registry.get(name)

    fun <I, O> workflow(name: String): KiwiFlowWorkflow<I, O> =
        registry.workflow(name)
}

val Application.KiwiFlow: KiwiFlowApplication
    get() = attributes[KiwiFlowApplicationKey]