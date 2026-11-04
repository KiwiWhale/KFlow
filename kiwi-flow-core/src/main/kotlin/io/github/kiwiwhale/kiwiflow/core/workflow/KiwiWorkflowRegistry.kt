package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiregistry.KiwiRegistry
import io.github.kiwiwhale.kiwiregistry.RegistryId

/**
 * Registry for managing KiwiWorkflow instances.
 *
 * Supports typed keys and RegistryId-based access.
 *
 * @see KiwiWorkflow
 * @see KiwiWorkflowKey
 */
class KiwiWorkflowRegistry {

    private val registry = KiwiRegistry(::createKey)

    val size: Int
        get() = registry.size

    fun register(workflow: KiwiWorkflow<*, *>) {
        registry.register(workflow.key, workflow)
    }

    fun register(
        key: KiwiWorkflowKey<*, *>,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(key.id == workflow.key.id) {
            "Workflow key '${key.id}' does not match " +
                    "workflow key '${workflow.key.id}'."
        }

        registry.register(key, workflow)
    }

    fun register(
        name: String,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(name == workflow.key.name) {
            "Workflow name '$name' does not match " +
                    "workflow key '${workflow.key.name}'."
        }

        register(workflow)
    }

    fun registerAll(
        additions: Collection<KiwiWorkflow<*, *>>
    ) {
        registry.registerAll(
            additions.associateBy { it.key }
        )
    }

    operator fun get(
        key: KiwiWorkflowKey<*, *>
    ): KiwiWorkflow<*, *> =
        registry[key]

    operator fun get(
        id: RegistryId
    ): KiwiWorkflow<*, *> =
        registry[id]

    operator fun get(
        name: String
    ): KiwiWorkflow<*, *> =
        registry[id(name)]

    fun getOrNull(
        key: KiwiWorkflowKey<*, *>
    ): KiwiWorkflow<*, *>? =
        registry.getOrNull(key)

    fun getOrNull(
        id: RegistryId
    ): KiwiWorkflow<*, *>? =
        registry.getOrNull(id)

    fun getOrNull(
        name: String
    ): KiwiWorkflow<*, *>? =
        registry.getOrNull(id(name))

    operator fun contains(
        key: KiwiWorkflowKey<*, *>
    ): Boolean =
        registry.contains(key)

    operator fun contains(
        id: RegistryId
    ): Boolean =
        registry.contains(id)

    operator fun contains(
        name: String
    ): Boolean =
        registry.contains(id(name))

    fun unregister(
        key: KiwiWorkflowKey<*, *>
    ): Boolean =
        registry.unregister(key)

    fun unregister(
        id: RegistryId
    ): Boolean =
        registry.unregister(id)

    fun unregister(
        name: String
    ): Boolean =
        registry.unregister(id(name))

    fun keys(): Set<KiwiWorkflowKey<*, *>> =
        registry.keys()

    fun ids(): Set<RegistryId> =
        registry.ids()

    fun names(): Set<String> =
        ids().mapTo(LinkedHashSet()) { it.key }

    fun snapshot(): Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>> =
        registry.snapshot()

    private fun createKey(
        id: RegistryId
    ): KiwiWorkflowKey<*, *> {
        require(id.namespace == NAMESPACE) {
            "Invalid workflow namespace: '${id.namespace}'."
        }

        return KiwiWorkflowKey<Any?, Any?>(id.key)
    }

    private fun id(name: String): RegistryId =
        RegistryId(NAMESPACE, name)

    private companion object {
        const val NAMESPACE = "kiwi-flow"
    }
}
