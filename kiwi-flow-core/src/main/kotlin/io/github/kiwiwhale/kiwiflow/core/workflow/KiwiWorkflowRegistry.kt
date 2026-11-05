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

    @Suppress("UNCHECKED_CAST")
    operator fun <I, O> get(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> =
        get(key.name) as KiwiWorkflow<I, O>

    operator fun get(name: String): KiwiWorkflow<*, *>? =
        registry[name]

    fun getOrNull(
        key: KiwiWorkflowKey<*, *>
    ): KiwiWorkflow<*, *>? =
        registry.getOrNull(key.name)

    fun getOrNull(name: String): KiwiWorkflow<*, *>? =
        registry.getOrNull(name)

    operator fun contains(
        key: KiwiWorkflowKey<*, *>
    ): Boolean =
        key.name in registry

    operator fun contains(name: String): Boolean =
        name in registry

    fun keys(): Set<String> =
        registry.keys

    fun values(): Collection<KiwiWorkflow<*, *>> =
        registry.values

    fun <I, O> workflow(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> =
        get(key)

    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(name: String): KiwiWorkflow<I, O> =
        get(name) as KiwiWorkflow<I, O>
}
