package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiflow.core.KiwiFlowDsl
import io.github.kiwiwhale.kiwiflow.core.context.WorkflowContext
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionTracker
import io.github.kiwiwhale.kiwiflow.core.step.StepBuilder

/**
 * Provides the execution scope for a KiwiFlow workflow.
 *
 * Exposes the workflow input, shared execution context,
 * and DSL functions used to declare and execute steps.
 *
 * Each workflow execution receives its own scope and context.
 *
 * @param I Workflow input type.
 * @property input Input supplied to the current workflow execution.
 * @property context Shared key-value context for workflow steps.
 */
@KiwiFlowDsl
class KiwiFlowWorkflowScope<I> internal constructor(
    val input: I,
    internal val tracker: ExecutionTracker,
    val context: WorkflowContext = WorkflowContext()
) {

    /**
     * Builds and executes a named workflow step.
     *
     * The step is configured through [StepBuilder] and executed
     * immediately within the current workflow execution.
     *
     * @param T Step output type.
     * @param name Unique step name within the workflow.
     * @param block Step configuration DSL.
     * @return Result produced by the step.
     * @throws IllegalArgumentException If the step name is blank.
     * @throws IllegalStateException If the step action is missing
     * or a configuration is duplicated.
     * @throws KiwiFlowException If step execution fails.
     */
    suspend fun <T> step(
        name: String,
        block: StepBuilder<I, T>.() -> Unit
    ): T {
        require(name.isNotBlank()) {
            "Step name cannot be blank"
        }

        val builder = StepBuilder<I, T>().apply(block)

        return builder.build(name).execute(this, tracker)
    }
}