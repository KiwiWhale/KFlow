package io.github.kiwiwhale.kiwiflow.core.workflow

import io.github.kiwiwhale.kiwiflow.core.KiwiFlowDsl
import io.github.kiwiwhale.kiwiflow.core.context.WorkflowContext
import io.github.kiwiwhale.kiwiflow.core.execution.ExecutionTracker
import io.github.kiwiwhale.kiwiflow.core.step.StepBuilder

@KiwiFlowDsl
class KiwiFlowWorkflowScope<I> internal constructor(
    val input: I,
    internal val tracker: ExecutionTracker,
    val context: WorkflowContext = WorkflowContext()
) {

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