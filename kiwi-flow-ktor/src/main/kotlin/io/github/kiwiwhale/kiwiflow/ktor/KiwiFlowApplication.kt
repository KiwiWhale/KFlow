package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import io.github.kiwiwhale.kiwiflow.ktor.registry.WorkflowRegistry
import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

internal val KiwiFlowApplicationKey =
    AttributeKey<KiwiFlowApplication>("KiwiFlowApplication")

/**
 * Provides access to workflows registered in a Ktor application.
 *
 * Acts as a read-only facade over the application's workflow registry.
 *
 * An instance is created when [KiwiFlowPlugin] is installed.
 *
 * @property registry Internal workflow registry.
 */
class KiwiFlowApplication internal constructor(
    private val registry: WorkflowRegistry
) {

    /**
     * Returns the number of registered workflows.
     */
    val size: Int
        get() = registry.size

    /**
     * Returns all registered workflow names.
     */
    fun names(): Set<String> =
        registry.names()

    /**
     * Checks whether a workflow is registered.
     *
     * @param name Workflow registration name.
     */
    fun contains(name: String): Boolean =
        registry.contains(name)

    /**
     * Retrieves a workflow by its registration name.
     *
     * @param name Workflow registration name.
     * @throws NoSuchElementException If the workflow is not registered.
     */
    fun get(name: String): KiwiFlowWorkflow<*, *> =
        registry.get(name)

    /**
     * Retrieves a workflow using the requested generic types.
     *
     * @param I Expected input type.
     * @param O Expected output type.
     * @param name Workflow registration name.
     * @throws NoSuchElementException If the workflow is not registered.
     */
    fun <I, O> workflow(
        name: String
    ): KiwiFlowWorkflow<I, O> =
        registry.workflow(name)
}

/**
 * Provides access to the KiwiFlow integration for this application.
 *
 * This property is available after [KiwiFlowPlugin] is installed.
 *
 * @throws NoSuchElementException If the plugin has not been installed.
 */
val Application.KiwiFlow: KiwiFlowApplication
    get() = attributes[KiwiFlowApplicationKey]
