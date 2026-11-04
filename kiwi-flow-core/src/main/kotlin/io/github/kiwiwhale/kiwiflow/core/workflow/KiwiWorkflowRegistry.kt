package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiregistry.RegistryId
import io.github.kiwiwhale.kiwiregistry.core.FrozenRegistry
import io.github.kiwiwhale.kiwiregistry.core.MutableRegistry
import io.github.kiwiwhale.kiwiregistry.exception.DuplicateEntryException

/**
 * Registry for managing [KiwiWorkflow] instances.
 *
 * Workflow storage, lookup, duplicate detection,
 * thread safety, and atomic registration are delegated
 * to kiwi-registry.
 *
 * @see KiwiWorkflow
 * @see KiwiWorkflowKey
 */
class KiwiWorkflowRegistry {

    private val registry =
        MutableRegistry.create<KiwiWorkflow<*, *>>(
            RegistryId(
                KiwiWorkflowKey.NAMESPACE,
                "workflows"
            )
        )

    /** Number of registered workflows. */
    val size: Int
        get() = registry.size

    /** Whether the registry contains no workflows. */
    val isEmpty: Boolean
        get() = registry.isEmpty

    /**
     * Registers a workflow using its own key.
     *
     * @param workflow Workflow to register.
     * @throws IllegalStateException If the workflow is already registered.
     */
    fun register(workflow: KiwiWorkflow<*, *>) {
        safely {
            registry.register(workflow.key.registryKey, workflow)
        }
    }

    /**
     * Registers a workflow using an explicit typed key.
     *
     * @param key Key used to identify the workflow.
     * @param workflow Workflow to register.
     * @throws IllegalArgumentException If the key does not match the workflow key.
     * @throws IllegalStateException If the workflow is already registered.
     */
    fun register(
        key: KiwiWorkflowKey<*, *>,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(key == workflow.key) {
            "Workflow key '${key.name}' does not match " +
                    "workflow key '${workflow.name}'."
        }

        register(workflow)
    }

    /**
     * Registers a workflow using an explicit name.
     *
     * @param name Name used to identify the workflow.
     * @param workflow Workflow to register.
     * @throws IllegalArgumentException If the name is blank or does not match.
     * @throws IllegalStateException If the workflow is already registered.
     */
    fun register(
        name: String,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }

        require(name == workflow.name) {
            "Workflow name '$name' does not match " +
                    "workflow name '${workflow.name}'."
        }

        register(workflow)
    }

    /**
     * Registers multiple workflows atomically.
     *
     * Duplicate names within the collection are rejected.
     *
     * @param workflows Workflows to register.
     * @throws IllegalArgumentException If the collection contains duplicate names.
     * @throws IllegalStateException If any workflow is already registered.
     */
    fun registerAll(workflows: Collection<KiwiWorkflow<*, *>>) {
        val duplicates = workflows
            .groupingBy { it.name }
            .eachCount()
            .filterValues { it > 1 }
            .keys

        require(duplicates.isEmpty()) {
            "Duplicate workflow names: ${duplicates.joinToString()}"
        }

        safely {
            registry.registerAll(
                workflows.associateBy { it.name }
            )
        }
    }

    /**
     * Registers multiple workflows using typed keys atomically.
     *
     * @param workflows Mapping of workflow keys to workflows.
     * @throws IllegalArgumentException If any key does not match its workflow.
     * @throws IllegalStateException If any workflow is already registered.
     */
    fun registerAllByKey(
        workflows: Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>
    ) {
        workflows.forEach { (key, workflow) ->
            require(key == workflow.key) {
                "Workflow key '${key.name}' does not match " +
                        "workflow key '${workflow.name}'."
            }
        }

        safely {
            registry.registerAllByKey(
                workflows.mapKeys { it.key.registryKey }
            )
        }
    }

    /**
     * Registers multiple workflows using string names atomically.
     *
     * @param workflows Mapping of workflow names to workflows.
     * @throws IllegalArgumentException If a name is blank or does not match.
     * @throws IllegalStateException If any workflow is already registered.
     */
    fun registerAllByName(
        workflows: Map<String, KiwiWorkflow<*, *>>
    ) {
        workflows.forEach { (name, workflow) ->
            require(name.isNotBlank()) {
                "Workflow name cannot be blank."
            }

            require(name == workflow.name) {
                "Workflow name '$name' does not match " +
                        "workflow name '${workflow.name}'."
            }
        }

        safely {
            registry.registerAll(workflows)
        }
    }

    /**
     * Retrieves a workflow using its typed key.
     *
     * @param key Workflow key.
     * @return Registered workflow.
     * @throws NoSuchElementException If the workflow does not exist.
     *
     * The caller must provide a key with matching input/output types.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <I, O> get(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> =
        registry[key.registryKey] as KiwiWorkflow<I, O>

    /**
     * Retrieves a workflow using its name.
     *
     * @param name Workflow name.
     * @return Registered workflow.
     * @throws NoSuchElementException If the workflow does not exist.
     */
    operator fun get(name: String): KiwiWorkflow<*, *> =
        registry[name]

    /**
     * Retrieves a workflow using its typed key.
     *
     * @param key Workflow key.
     * @return Registered workflow, or null if it does not exist.
     */
    fun getOrNull(
        key: KiwiWorkflowKey<*, *>
    ): KiwiWorkflow<*, *>? =
        registry.getOrNull(key.registryKey)

    /**
     * Retrieves a workflow using its name.
     *
     * @param name Workflow name.
     * @return Registered workflow, or null if it does not exist.
     */
    fun getOrNull(name: String): KiwiWorkflow<*, *>? =
        registry.getOrNull(name)

    /**
     * Checks whether a workflow with the given key exists.
     *
     * @param key Workflow key.
     */
    operator fun contains(key: KiwiWorkflowKey<*, *>): Boolean =
        key.registryKey in registry

    /**
     * Checks whether a workflow with the given name exists.
     *
     * @param name Workflow name.
     */
    operator fun contains(name: String): Boolean =
        name in registry

    /**
     * Returns all registered workflow names.
     */
    fun names(): Set<String> =
        registry.keys()

    /**
     * Returns all registered workflow keys.
     */
    fun keys(): Set<KiwiWorkflowKey<*, *>> =
        registry.values().mapTo(LinkedHashSet()) { it.key }

    /**
     * Returns an immutable snapshot of registered workflows.
     *
     * @return Mapping of workflow keys to workflows.
     */
    fun snapshot(): Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>> =
        registry.values().associateBy { it.key }

    /**
     * Creates an immutable registry snapshot.
     *
     * @return Frozen workflow registry.
     */
    fun freeze(): FrozenRegistry<KiwiWorkflow<*, *>> =
        registry.freeze()

    /**
     * Retrieves a workflow using its typed key.
     *
     * @param key Workflow key.
     */
    fun <I, O> workflow(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> = get(key)

    /**
     * Retrieves a workflow by name with the expected types.
     *
     * @param name Workflow name.
     *
     * The caller is responsible for providing the correct types.
     */
    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(name: String): KiwiWorkflow<I, O> =
        get(name) as KiwiWorkflow<I, O>

    /**
     * Converts registry duplicate exceptions into workflow-level exceptions.
     */
    private inline fun <T> safely(
        block: () -> T
    ): T =
        try {
            block()
        } catch (exception: DuplicateEntryException) {
            throw IllegalStateException(
                "Workflow is already registered.",
                exception
            )
        }
}
