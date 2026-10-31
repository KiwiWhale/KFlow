package io.github.mrjimin.kflow.ktor

import io.github.mrjimin.kflow.core.workflow.KFlowWorkflow
import io.github.mrjimin.kflow.ktor.registry.WorkflowRegistry
import io.ktor.server.application.*
import io.ktor.util.*

internal val KFlowApplicationKey =
    AttributeKey<KFlowApplication>("KFlowApplication")

class KFlowApplication internal constructor(
    val registry: WorkflowRegistry
) {

    val size: Int
        get() = registry.size

    fun names(): Set<String> =
        registry.names()

    fun contains(name: String): Boolean =
        registry.contains(name)

    fun get(name: String): KFlowWorkflow<*, *> =
        registry.get(name)

    fun <I, O> workflow(name: String): KFlowWorkflow<I, O> =
        registry.workflow(name)
}

val Application.kFlow: KFlowApplication
    get() = attributes[KFlowApplicationKey]