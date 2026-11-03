package io.github.kiwiwhale.kiwiflow.ktor.config

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry

/**
 * Configuration for registering workflows in a Ktor application.
 *
 * Workflow registration, validation, and duplicate detection
 * are delegated to Core.
 */
class KiwiFlowConfig {

    internal val registry = KiwiWorkflowRegistry()

    fun workflow(workflow: KiwiWorkflow<*, *>) {
        registry.register(workflow)
    }

    fun workflow(
        key: KiwiWorkflowKey<*, *>,
        workflow: KiwiWorkflow<*, *>
    ) {
        registry.register(key, workflow)
    }

    fun workflow(
        name: String,
        workflow: KiwiWorkflow<*, *>
    ) {
        registry.register(name, workflow)
    }

    fun workflows(workflows: Collection<KiwiWorkflow<*, *>>) {
        registry.registerAll(workflows)
    }

    /**
     * Registers multiple workflows using typed keys.
     */
    fun workflowsByKey(
        workflows: Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>
    ) {
        registry.registerAll(workflows)
    }

    /**
     * Registers multiple workflows using string names.
     */
    fun workflowsByName(
        workflows: Map<String, KiwiWorkflow<*, *>>
    ) {
        registry.registerAll(workflows)
    }

    internal fun registeredWorkflows():
            Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>> =
        registry.snapshot()
}
