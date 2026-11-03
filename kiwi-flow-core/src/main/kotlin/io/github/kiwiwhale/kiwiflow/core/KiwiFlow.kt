package io.github.kiwiwhale.kiwiflow.core

import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionListener
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflowScope

/**
 * Entry point for defining KiwiFlow workflows.
 *
 * Provides a DSL function for creating executable workflows
 * with an associated name and optional execution listeners.
 *
 * Example:
 *
 * ```
 * val workflow = KiwiFlow.workflow<String, String>(
 *     name = "example"
 * ) {
 *     step("uppercase") {
 *         execute {
 *             input.uppercase()
 *         }
 *     }
 * }
 *
 * val result = workflow.execute("hello")
 * ```
 */
object KiwiFlow {

    /**
     * Creates a workflow using the KiwiFlow DSL.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param name Non-blank workflow name.
     * @param listeners Listeners that observe workflow lifecycle events.
     * @param block Workflow execution logic.
     * @return Configured executable workflow.
     * @throws IllegalArgumentException If the workflow name is blank.
     */
    fun <I, O> workflow(
        name: String,
        listeners: List<ExecutionListener> = emptyList(),
        block: suspend KiwiFlowWorkflowScope<I>.() -> O
    ): KiwiFlowWorkflow<I, O> {
        require(name.isNotBlank()) {
            "Workflow name cannot be blank"
        }

        return KiwiFlowWorkflow(name, block, listeners.toList())
    }
}