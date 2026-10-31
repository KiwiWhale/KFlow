package io.github.mrjimin.kflow.core.workflow

import io.github.mrjimin.kflow.core.KFlowDsl
import io.github.mrjimin.kflow.core.context.WorkflowContext
import io.github.mrjimin.kflow.core.execution.ExecutionTracker
import io.github.mrjimin.kflow.core.step.StepBuilder

@KFlowDsl
class KFlowWorkflowScope<I> internal constructor(
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