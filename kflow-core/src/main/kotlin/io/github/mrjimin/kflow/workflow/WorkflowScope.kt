package io.github.mrjimin.kflow.workflow

import io.github.mrjimin.kflow.KFlowDsl
import io.github.mrjimin.kflow.context.WorkflowContext
import io.github.mrjimin.kflow.execution.ExecutionTracker
import io.github.mrjimin.kflow.step.StepBuilder

@KFlowDsl
class WorkflowScope<I> internal constructor(
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