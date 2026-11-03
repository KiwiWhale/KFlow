package io.github.kiwiwhale.kiwiflow.ktor.config

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import java.util.Collections

/**
 * Configuration for registering KiwiFlow workflows in a Ktor application.
 *
 * Workflows registered through this configuration become available
 * through [KiwiFlowApplication]
 * after [KiwiFlowPlugin] is installed.
 *
 * Workflow names must be non-blank and unique.
 */
class KiwiFlowConfig {

    private val registered =
        linkedMapOf<String, KiwiFlowWorkflow<*, *>>()

    /**
     * Registers a workflow under the specified name.
     *
     * @param name Unique workflow registration name.
     * @param workflow Workflow instance.
     * @throws IllegalArgumentException If the name is blank.
     * @throws IllegalStateException If the name is already registered.
     */
    fun workflow(
        name: String,
        workflow: KiwiFlowWorkflow<*, *>
    ) {
        validateName(name)

        check(registered.putIfAbsent(name, workflow) == null) {
            "Workflow '$name' is already registered."
        }
    }

    /**
     * Registers multiple workflows.
     *
     * All entries are validated before modifying the configuration.
     * If validation fails, no workflow from this operation is registered.
     *
     * @param workflows Workflows keyed by their registration names.
     * @throws IllegalArgumentException If any name is blank.
     * @throws IllegalStateException If any name is already registered.
     */
    fun workflows(
        workflows: Map<String, KiwiFlowWorkflow<*, *>>
    ) {
        workflows.keys.forEach(::validateName)

        val duplicates = workflows.keys
            .filter { it in registered }
            .sorted()

        check(duplicates.isEmpty()) {
            "Workflows already registered: ${duplicates.joinToString()}."
        }

        registered.putAll(workflows)
    }

    /**
     * Returns an immutable snapshot of registered workflows.
     *
     * @return Workflow registrations in insertion order.
     */
    internal fun registeredWorkflows(): Map<String, KiwiFlowWorkflow<*, *>> =
        Collections.unmodifiableMap(
            LinkedHashMap(registered)
        )

    private fun validateName(name: String) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
    }
}
