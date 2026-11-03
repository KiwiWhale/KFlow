package io.github.kiwiwhale.kiwiflow.core.step

import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflowScope
import kotlin.time.Duration

/**
 * DSL builder for configuring an executable workflow step.
 *
 * Allows configuring retry behavior, execution timeout,
 * and the step action.
 *
 * Each configuration can only be defined once.
 *
 * @param I Workflow input type.
 * @param O Step output type.
 */
class StepBuilder<I, O> internal constructor() {

    private var action: (suspend KiwiFlowWorkflowScope<I>.() -> O)? = null

    private var policy = StepPolicy()

    /**
     * Configures the retry policy for this step.
     *
     * @param policy Retry policy to apply.
     * @throws IllegalStateException If a retry policy was already configured.
     */
    fun retry(policy: RetryPolicy) {
        check(this.policy.retry == null) {
            "Retry policy is already configured"
        }

        this.policy = this.policy.copy(
            retry = policy
        )
    }

    /**
     * Configures the maximum execution duration for this step.
     *
     * @param duration Maximum execution duration.
     * @throws IllegalStateException If a timeout was already configured.
     * @throws IllegalArgumentException If the duration is not finite
     * or positive.
     */
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

    /**
     * Defines the action executed by this step.
     *
     * @param block Step execution logic.
     * @throws IllegalStateException If an action was already defined.
     */
    fun execute(block: suspend KiwiFlowWorkflowScope<I>.() -> O) {
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