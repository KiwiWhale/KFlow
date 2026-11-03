package io.github.kiwiwhale.kiwiflow.core.workflow

import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe registry for managing KiwiWorkflow instances.
 *
 * Workflows are identified exclusively by KiwiWorkflowKey.
 *
 * String-based registration and lookup are also supported.
 *
 * Registration uses copy-on-write semantics.
 * Multiple registrations are atomic.
 */
class KiwiWorkflowRegistry {

    private val workflows =
        AtomicReference<Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>>(
            emptyMap()
        )

    val size: Int
        get() = workflows.get().size

    fun register(workflow: KiwiWorkflow<*, *>) {
        registerEntries(mapOf(workflow.key to workflow))
    }

    fun register(
        key: KiwiWorkflowKey<*, *>,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(key == workflow.key) {
            "Workflow key '${key.name}' does not match " +
                    "workflow key '${workflow.key.name}'."
        }

        register(workflow)
    }

    fun register(
        name: String,
        workflow: KiwiWorkflow<*, *>
    ) {
        validateName(name)

        require(name == workflow.key.name) {
            "Workflow name '$name' does not match " +
                    "workflow key '${workflow.key.name}'."
        }

        register(workflow)
    }

    fun registerAll(
        additions: Collection<KiwiWorkflow<*, *>>
    ) {
        registerEntries(
            additions.associateBy { it.key }
        )
    }

    fun registerAll(
        additions: Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>
    ) {
        additions.forEach { (key, workflow) ->
            require(key == workflow.key) {
                "Workflow key '${key.name}' does not match " +
                        "workflow key '${workflow.key.name}'."
            }
        }

        registerEntries(LinkedHashMap(additions))
    }

    @JvmName("registerAllByName")
    fun registerAll(
        additions: Map<String, KiwiWorkflow<*, *>>
    ) {
        val entries =
            LinkedHashMap<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>()

        additions.forEach { (name, workflow) ->
            validateName(name)

            require(name == workflow.key.name) {
                "Workflow name '$name' does not match " +
                        "workflow key '${workflow.key.name}'."
            }

            check(entries.putIfAbsent(workflow.key, workflow) == null) {
                "Workflow '${workflow.key.name}' is registered multiple times."
            }
        }

        registerEntries(entries)
    }

    private fun registerEntries(
        additions: Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>
    ) {
        if (additions.isEmpty()) return

        while (true) {
            val current = workflows.get()

            val duplicates = additions.keys
                .filter { it in current }
                .map { it.name }
                .sorted()

            check(duplicates.isEmpty()) {
                "Workflows already registered: ${duplicates.joinToString()}."
            }

            val updated = LinkedHashMap(current)
            updated.putAll(additions)

            val immutable = Collections.unmodifiableMap(updated)

            if (workflows.compareAndSet(current, immutable)) {
                return
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <I, O> get(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> =
        getOrNull(key)
            ?: throw NoSuchElementException(
                "Workflow '${key.name}' is not registered."
            )

    fun get(name: String): KiwiWorkflow<*, *> =
        getOrNull(name)
            ?: throw NoSuchElementException(
                "Workflow '$name' is not registered."
            )

    @Suppress("UNCHECKED_CAST")
    fun <I, O> getOrNull(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O>? =
        workflows.get()[key] as? KiwiWorkflow<I, O>

    fun getOrNull(name: String): KiwiWorkflow<*, *>? {
        validateName(name)

        return workflows.get()[
            KiwiWorkflowKey<Any?, Any?>(name)
        ]
    }

    fun contains(key: KiwiWorkflowKey<*, *>): Boolean =
        workflows.get().containsKey(key)

    fun contains(name: String): Boolean {
        validateName(name)

        return workflows.get().containsKey(
            KiwiWorkflowKey<Any?, Any?>(name)
        )
    }

    fun keys(): Set<KiwiWorkflowKey<*, *>> =
        Collections.unmodifiableSet(
            LinkedHashSet(workflows.get().keys)
        )

    fun names(): Set<String> =
        Collections.unmodifiableSet(
            workflows.get().keys.mapTo(LinkedHashSet()) { it.name }
        )

    fun snapshot(): Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>> =
        workflows.get()

    fun <I, O> workflow(
        key: KiwiWorkflowKey<I, O>
    ): KiwiWorkflow<I, O> = get(key)

    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(name: String): KiwiWorkflow<I, O> =
        get(name) as KiwiWorkflow<I, O>

    private fun validateName(name: String) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }
}
