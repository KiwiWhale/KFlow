package io.github.kiwiwhale.kiwiflow.ktor.config

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiflow.core.workflow.bootstrap.KiwiWorkflowBootstrap
import io.github.kiwiwhale.kiwiflow.core.workflow.bootstrap.KiwiWorkflowHolder

/**
 * Configuration for registering workflows in a Ktor application.
 *
 * Workflow definitions are collected during configuration
 * and submitted to kiwi-registry during bootstrap.
 */
class KiwiFlowConfig {

    private val workflows = LinkedHashMap<String, KiwiWorkflow<*, *>>()

    /**
     * Registers a workflow.
     */
    fun workflow(workflow: KiwiWorkflow<*, *>) {
        registerWorkflow(workflow)
    }

    /**
     * Registers a workflow using an explicit typed key.
     */
    fun workflow(
        key: KiwiWorkflowKey<*, *>,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(key == workflow.key) {
            "Workflow key '${key.name}' does not match workflow key '${workflow.name}'."
        }
        registerWorkflow(workflow)
    }

    /**
     * Registers a workflow using its name.
     */
    fun workflow(
        name: String,
        workflow: KiwiWorkflow<*, *>
    ) {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank."
        }
        require(name == workflow.name) {
            "Workflow name '$name' does not match workflow name '${workflow.name}'."
        }
        registerWorkflow(workflow)
    }

    /**
     * Registers multiple workflows with atomic rollback on failure.
     */
    fun workflows(
        workflows: Collection<KiwiWorkflow<*, *>>
    ) {
        val duplicates = workflows
            .groupingBy { it.name }
            .eachCount()
            .filterValues { it > 1 }
            .keys

        check(duplicates.isEmpty()) {
            "Duplicate workflow names in input: ${duplicates.joinToString()}"
        }

        for (wf in workflows) {
            check(!this.workflows.containsKey(wf.name)) {
                "Workflow '${wf.name}' is already registered."
            }
        }

        workflows.forEach(::registerWorkflow)
    }

    /**
     * Registers multiple workflows using typed keys.
     */
    fun workflowsByKey(
        workflows: Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>
    ) {
        workflows.forEach { (key, workflow) ->
            require(key == workflow.key) {
                "Workflow key '${key.name}' does not match workflow key '${workflow.name}'."
            }
        }
        this.workflows(workflows.values)
    }

    /**
     * Registers multiple workflows using names.
     */
    fun workflowsByName(
        workflows: Map<String, KiwiWorkflow<*, *>>
    ) {
        workflows.forEach { (name, workflow) ->
            require(name.isNotBlank()) {
                "Workflow name cannot be blank."
            }
            require(name == workflow.name) {
                "Workflow name '$name' does not match workflow name '${workflow.name}'."
            }
        }
        this.workflows(workflows.values)
    }

    /**
     * Returns an immutable snapshot of registered workflows mapped by typed key.
     */
    fun registeredWorkflows(): Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>> =
        workflows.values.associateBy { it.key }

    /**
     * Submits collected workflows to the global Core bootstrap holder.
     * Executes the exact inject -> contribute -> build lifecycle.
     */
    internal fun registerContributions() {
        val build = KiwiWorkflowBootstrap.inject()

        KiwiWorkflowHolder.registryBootstrap(KiwiWorkflowBootstrap) {
            registry(KiwiWorkflowKey.WORKFLOWS) {
                workflows.forEach { (name, workflow) ->
                    add(name, workflow)
                }
            }
        }

        build()
    }

    private fun registerWorkflow(workflow: KiwiWorkflow<*, *>) {
        check(!workflows.containsKey(workflow.name)) {
            "Workflow '${workflow.name}' is already registered."
        }
        workflows[workflow.name] = workflow
    }
}