package io.github.mrjimin.kflow.core.step

import io.github.mrjimin.kflow.core.policy.RetryPolicy
import io.github.mrjimin.kflow.core.workflow.KFlowWorkflowScope
import kotlin.time.Duration

class StepBuilder<I, O> internal constructor() {

    private var action: (suspend KFlowWorkflowScope<I>.() -> O)? = null

    private var policy = StepPolicy()

    fun retry(policy: RetryPolicy) {
        check(this.policy.retry == null) {
            "Retry policy is already configured"
        }

        this.policy = this.policy.copy(
            retry = policy
        )
    }

    fun timeout(duration: Duration) {
        check(this.policy.timeout == null) {
            "Timeout is already configured"
        }

        require(duration.isFinite() && duration.isPositive()) {
            "Timeout must be finite and positive"
        }

        policy = policy.copy(
            timeout = duration
        )
    }

    fun execute(block: suspend KFlowWorkflowScope<I>.() -> O) {
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