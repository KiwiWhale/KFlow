package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiflow.core.KiwiFlowDsl
import io.github.kiwiwhale.kiwiflow.core.context.WorkflowContext
import io.github.kiwiwhale.kiwiflow.core.exception.KiwiFlowException
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionTracker
import io.github.kiwiwhale.kiwiflow.core.step.StepBuilder

/**
 * Provides the execution scope for a KiwiWorkflow.
 *
 * Exposes the workflow input, shared execution context,
 * and DSL functions used to configure and execute workflow steps.
 *
 * A new scope and [WorkflowContext] are created for each
 * workflow execution.
 *
 * @param I Workflow input type.
 * @property input Input supplied to the current workflow execution.
 * @property context Shared key-value context available to workflow steps.
 *
 * @see KiwiWorkflow
 * @see WorkflowContext
 */
@KiwiFlowDsl
class KiwiWorkflowScope<I> internal constructor(
    val input: I,
    internal val tracker: ExecutionTracker,
    val context: WorkflowContext = WorkflowContext()
) {

    /**
     * Configures and executes a workflow step.
     *
     * The supplied block configures a [StepBuilder].
     * After configuration, the step is built and executed immediately.
     *
     * Steps are executed sequentially unless concurrency is
     * explicitly introduced by the workflow implementation.
     *
     * @param T Step output type.
     * @param name Unique step name within the workflow.
     * @param block Step configuration DSL.
     * @return Output produced by the step.
     *
     * @throws IllegalArgumentException If the step name is blank.
     * @throws IllegalStateException If the step configuration is invalid.
     * @throws KiwiFlowException If step execution fails.
     * @throws kotlinx.coroutines.CancellationException If execution
     * is cancelled.
     *
     * @see StepBuilder
     */
    suspend fun <T> step(
        name: String,
        block: StepBuilder<I, T>.() -> Unit
    ): T {
        require(name.isNotBlank()) {
            "Step name cannot be blank."
        }

        val builder = StepBuilder<I, T>().apply(block)

        val step = builder.build(name)

        return step.execute(
            scope = this,
            tracker = tracker
        )
    }
}
