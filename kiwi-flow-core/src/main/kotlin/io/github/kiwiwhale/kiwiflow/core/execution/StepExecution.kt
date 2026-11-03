package io.github.kiwiwhale.kiwiflow.core.execution

import kotlin.time.Duration

/**
 * Represents the recorded outcome of a single step execution attempt.
 *
 * Each retry attempt produces a separate StepExecution record.
 *
 * @property name Name of the executed step.
 * @property status Execution outcome.
 * @property duration Time spent executing the step.
 * @property attempt Attempt number, starting at one.
 * @property error Failure associated with the attempt, if any.
 */
data class StepExecution(
    val name: String,
    val status: ExecutionStatus,
    val duration: Duration,
    val attempt: Int = 1,
    val error: Throwable? = null
) {

    init {
        require(name.isNotBlank()) {
            "Step name cannot be blank"
        }

        require(attempt >= 1) {
            "Attempt must be at least 1"
        }

        require(duration >= Duration.ZERO) {
            "Duration cannot be negative"
        }
    }

    /**
     * Whether the step execution completed successfully.
     */
    val successful: Boolean
        get() = status == ExecutionStatus.SUCCESS

    /**
     * Whether the step execution failed.
     */
    val isFailure: Boolean
        get() = status == ExecutionStatus.FAILED
}
