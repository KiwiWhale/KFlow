package io.github.mrjimin.kflow.step

import io.github.mrjimin.kflow.policy.RetryPolicy
import io.github.mrjimin.kflow.workflow.WorkflowScope
import kotlin.time.Duration

class StepBuilder<I, O> internal constructor() {

    private var action: (suspend WorkflowScope<I>.() -> O)? = null

    private var policy = StepPolicy()

    fun retry(policy: RetryPolicy) {
        this.policy = this.policy.copy(retry = policy)
    }

    fun timeout(duration: Duration) {
        require(duration.isPositive()) {
            "Timeout must be positive"
        }

        policy = policy.copy(timeout = duration)
    }

    fun execute(block: suspend WorkflowScope<I>.() -> O) {
        check(action == null) {
            "Step action is already defined"
        }

        action = block
    }

    internal fun build(name: String): Step<I, O> {
        require(name.isNotBlank()) {
            "Step name cannot be blank"
        }

        return Step(
            name = name,
            action = checkNotNull(action) {
                "Step action is not defined: $name"
            },
            policy = policy
        )
    }
}