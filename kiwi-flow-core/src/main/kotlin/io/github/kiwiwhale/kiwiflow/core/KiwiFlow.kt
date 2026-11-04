package io.github.kiwiwhale.kiwiflow.core

import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionListener
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowScope

/**
 * Entry point for defining KiwiFlow workflows.
 *
 * Provides DSL functions for creating executable workflows
 * using either a typed [KiwiWorkflowKey] or a workflow name.
 *
 * @see KiwiWorkflow
 * @see KiwiWorkflowKey
 */
object KiwiFlow {

    /**
     * Creates a workflow using a typed workflow key.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param key Typed workflow identifier.
     * @param listeners Execution event listeners.
     * @param block Workflow definition.
     * @return Configured workflow.
     *
     * @throws IllegalArgumentException If the workflow key has an invalid name.
     */
    fun <I, O> workflow(
        key: KiwiWorkflowKey<I, O>,
        listeners: List<ExecutionListener> = emptyList(),
        block: suspend KiwiWorkflowScope<I>.() -> O
    ): KiwiWorkflow<I, O> =
        KiwiWorkflow(
            key = key,
            action = block,
            listeners = listeners
        )

    /**
     * Creates a workflow using its name.
     *
     * Internally, the name is converted into a [KiwiWorkflowKey],
     * keeping workflow creation consistent with the typed API.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param name Workflow name.
     * @param listeners Execution event listeners.
     * @param block Workflow definition.
     * @return Configured workflow.
     *
     * @throws IllegalArgumentException If the workflow name is blank.
     */
    fun <I, O> workflow(
        name: String,
        listeners: List<ExecutionListener> = emptyList(),
        block: suspend KiwiWorkflowScope<I>.() -> O
    ): KiwiWorkflow<I, O> =
        workflow(
            key = KiwiWorkflowKey(name),
            listeners = listeners,
            block = block
        )
}

/**
 * Creates a workflow using a typed workflow key.
 *
 * A top-level convenience function that delegates to [KiwiFlow.workflow],
 * providing a concise DSL entry point for workflow creation.
 *
 * @param I Workflow input type.
 * @param O Workflow output type.
 * @param key Typed workflow identifier.
 * @param listeners Execution event listeners.
 * @param block Workflow definition.
 * @return Configured workflow.
 *
 * @throws IllegalArgumentException If the workflow key has an invalid name.
 *
 * @see KiwiFlow.workflow
 * @see KiwiWorkflowKey
 */
fun <I, O> kiwiFlow(
    key: KiwiWorkflowKey<I, O>,
    listeners: List<ExecutionListener> = emptyList(),
    block: suspend KiwiWorkflowScope<I>.() -> O
) = KiwiFlow.workflow(key, listeners, block)

/**
 * Creates a workflow using its name.
 *
 * A top-level convenience function that accepts a workflow name
 * and delegates to the typed [kiwiFlow] overload.
 *
 * Internally, the name is converted into a [KiwiWorkflowKey],
 * keeping workflow creation consistent with the typed API.
 *
 * @param I Workflow input type.
 * @param O Workflow output type.
 * @param name Workflow name.
 * @param listeners Execution event listeners.
 * @param block Workflow definition.
 * @return Configured workflow.
 *
 * @throws IllegalArgumentException If the workflow name is blank.
 *
 * @see KiwiFlow.workflow
 * @see KiwiWorkflowKey
 */
fun <I, O> kiwiFlow(
    name: String,
    listeners: List<ExecutionListener> = emptyList(),
    block: suspend KiwiWorkflowScope<I>.() -> O
): KiwiWorkflow<I, O> = kiwiFlow(KiwiWorkflowKey(name), listeners, block)