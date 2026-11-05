package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiflow.core.workflow.bootstrap.KiwiWorkflowBootstrap
import io.github.kiwiwhale.kiwiregistry.bootstrap.BootstrapHolder
import io.github.kiwiwhale.kiwiregistry.core.Registry

/**
 * Registry facade for accessing registered KiwiWorkflows.
 */
class KiwiWorkflowRegistry(
    private val bootstrap: BootstrapHolder = KiwiWorkflowBootstrap
) {

    private val registry: Registry<String, KiwiWorkflow<*, *>>
        get() = bootstrap[KiwiWorkflowKey.WORKFLOWS]

    val size: Int
        get() = registry.size

    val isEmpty: Boolean
        get() = registry.isEmpty

    val keys: Set<String>
        get() = registry.keys

    val values: Collection<KiwiWorkflow<*, *>>
        get() = registry.values

    operator fun contains(key: KiwiWorkflowKey<*, *>): Boolean =
        key.name in registry

    operator fun contains(name: String): Boolean =
        name in registry

    operator fun get(key: KiwiWorkflowKey<*, *>): KiwiWorkflow<*, *> =
        registry[key.name]
            ?: throw NoSuchElementException("Workflow '${key.name}' is not registered.")

    operator fun get(name: String): KiwiWorkflow<*, *>? =
        registry[name]

    fun getOrNull(key: KiwiWorkflowKey<*, *>): KiwiWorkflow<*, *>? =
        registry.getOrNull(key.name)

    fun getOrNull(name: String): KiwiWorkflow<*, *>? =
        registry.getOrNull(name)

    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> =
        get(key) as KiwiWorkflow<I, O>

    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(name: String): KiwiWorkflow<I, O> =
        registry[name] as? KiwiWorkflow<I, O>
            ?: throw NoSuchElementException("Workflow '$name' is not registered.")
}
