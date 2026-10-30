package io.github.mrjimin.kflow.execution

import kotlin.time.Duration

data class ExecutionResult<T>(
    val executionId: String,
    val workflowName: String,
    val status: ExecutionStatus,
    val value: T?,
    val steps: List<StepExecution>,
    val duration: Duration,
    val error: Throwable? = null
) {

    val successful: Boolean
        get() = status == ExecutionStatus.SUCCESS

    val isFailure: Boolean
        get() = status == ExecutionStatus.FAILED

    fun getOrThrow(): T {
        if (isFailure) {
            throw error ?: IllegalStateException(
                "Workflow execution failed: $workflowName"
            )
        }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    fun getOrNull(): T? =
        if (successful) value else null

    fun exceptionOrNull(): Throwable? =
        if (isFailure) error else null

    inline fun <R> fold(
        onSuccess: (T?) -> R,
        onFailure: (Throwable?) -> R
    ): R = when (status) {
        ExecutionStatus.SUCCESS -> onSuccess(value)
        ExecutionStatus.FAILED -> onFailure(error)
    }
}
