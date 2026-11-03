package io.github.kiwiwhale.kiwiflow.ktor.registry

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread-safe registry for managing named KiwiFlow workflows.
 *
 * Supports concurrent workflow registration and lookup.
 *
 * Registration operations use copy-on-write semantics to ensure
 * that each successful registration is applied atomically.
 *
 * Workflow instances are shared and are not copied.
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
     * The operation succeeds only if all workflow names are valid
     * and none of them have already been registered.
     *
     * If another registration modifies the registry concurrently,
     * the operation retries against the latest registry state.
     *
     * @param workflows Workflows keyed by their registration names.
     * @throws IllegalArgumentException If any name is blank.
     * @throws IllegalStateException If any name is already registered.
     */
    fun registerAll(
        workflows: Map<String, KiwiFlowWorkflow<*, *>>
    ) {
        workflows.keys.forEach(::validateName)

        if (workflows.isEmpty()) {
            return
        }

        while (true) {
            val current = this.workflows.get()

            val duplicates = workflows.keys
                .filter { it in current }
                .sorted()

            check(duplicates.isEmpty()) {
                "Workflows already registered: ${duplicates.joinToString()}."
            }

            val updated = LinkedHashMap(current)
            updated.putAll(workflows)

            val immutable = Collections.unmodifiableMap(updated)

            if (this.workflows.compareAndSet(current, immutable)) {
                return
            }
        }
    }

    /**
     * Retrieves a registered workflow by name.
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
     * @return Registered workflow names in insertion order.
     */
    fun names(): Set<String> =
        Collections.unmodifiableSet(
            LinkedHashSet(workflows.get().keys)
        )

    /**
     * Retrieves a workflow using the requested generic types.
     *
     * This method performs an unchecked cast because JVM type erasure
     * prevents verification of the actual input and output types.
     *
     * The caller must ensure that the requested types match
     * the registered workflow's actual types.
     *
     * @param I Expected workflow input type.
     * @param O Expected workflow output type.
     * @param name Workflow registration name.
     * @return Workflow cast to the requested generic types.
     * @throws NoSuchElementException If the workflow is not registered.
     */
    @Suppress("UNCHECKED_CAST")
    fun <I, O> workflow(name: String): KiwiFlowWorkflow<I, O> =
        get(name) as KiwiFlowWorkflow<I, O>

    private fun validateName(name: String) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }
}
