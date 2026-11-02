package io.github.kiwiwhale.kiwiflow.core.execution

import kotlin.time.Duration

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

    val successful: Boolean
        get() = status == ExecutionStatus.SUCCESS

    val isFailure: Boolean
        get() = status == ExecutionStatus.FAILED
}
