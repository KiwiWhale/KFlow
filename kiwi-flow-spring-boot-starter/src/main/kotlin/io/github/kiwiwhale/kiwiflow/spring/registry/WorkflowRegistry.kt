package io.github.kiwiwhale.kiwiflow.spring.registry

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe registry for managing named KiwiFlow workflows.
 *
 * Supports concurrent registration and lookup operations.
 *
 * Registration uses copy-on-write semantics, allowing readers
 * to access immutable registry snapshots without locking.
 *
 * @see KiwiFlowWorkflow
 */
class WorkflowRegistry {

    private val workflows =
        AtomicReference<Map<String, KiwiFlowWorkflow<*, *>>>(emptyMap())

    /**
     * Returns the number of registered workflows.
     */
    val size: Int
        get() = workflows.get().size

    /**
     * Registers a workflow under the specified name.
     *
     * @param name Unique workflow registration name.
     * @param workflow Workflow instance.
     * @throws IllegalArgumentException If the name is blank.
     * @throws IllegalStateException If the name is already registered.
     */
    fun register(
        name: String,
        workflow: KiwiFlowWorkflow<*, *>
    ) {
        registerAll(mapOf(name to workflow))
    }

    /**
     * Registers multiple workflows atomically.
     *
     * Either all workflows are registered successfully,
     * or none of them are registered.
     *
     * @param workflows Workflows keyed by their registration names.
     * @throws IllegalArgumentException If any name is blank.
     * @throws IllegalStateException If any name is already registered.
     */
    fun registerAll(
        workflows: Map<String, KiwiFlowWorkflow<*, *>>
    ) {
        val additions = LinkedHashMap(workflows)

        additions.keys.forEach(::validateName)

        if (additions.isEmpty()) return

        while (true) {
            val current = this.workflows.get()

            val duplicates = additions.keys
                .filter { it in current }
                .sorted()

            check(duplicates.isEmpty()) {
                "Workflows already registered: ${duplicates.joinToString()}."
            }

            val updated = LinkedHashMap(current)
            updated.putAll(additions)

            val immutable = Collections.unmodifiableMap(updated)

            if (this.workflows.compareAndSet(current, immutable)) {
                return
            }
        }
    }

    /**
     * Retrieves a registered workflow.
     *
     * @param name Workflow registration name.
     * @return Registered workflow.
     * @throws NoSuchElementException If the workflow is not registered.
     */
    fun get(name: String): KiwiFlowWorkflow<*, *> =
        getOrNull(name)
            ?: throw NoSuchElementException(
                "Workflow '$name' is not registered."
            )

    /**
     * Retrieves a workflow without throwing an exception.
     *
     * @param name Workflow registration name.
     * @return Registered workflow, or null if not found.
     */
    fun getOrNull(name: String): KiwiFlowWorkflow<*, *>? =
        workflows.get()[name]

    /**
     * Checks whether a workflow is registered.
     *
     * @param name Workflow registration name.
     * @return True if the workflow exists.
     */
    fun contains(name: String): Boolean =
        workflows.get().containsKey(name)

    /**
     * Returns an immutable snapshot of registered workflow names.
     *
     * @return Workflow names in insertion order.
     */
    fun names(): Set<String> =
        Collections.unmodifiableSet(
            LinkedHashSet(workflows.get().keys)
        )

    private fun validateName(name: String) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }
}
