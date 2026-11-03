package io.github.kiwiwhale.kiwiflow.core.execution

import kotlin.time.Duration

/**
 * Contains the final result of a workflow execution.
 *
 * Provides execution metadata, step execution records,
 * the resulting value, and failure information.
 *
 * A successful execution may contain a nullable value when
 * the workflow output type permits null.
 *
 * @param T Workflow output type.
 * @property executionId Unique identifier of this execution.
 * @property workflowName Name of the executed workflow.
 * @property status Final execution status.
 * @property value Workflow output, or null when unavailable.
 * @property steps Recorded step execution attempts.
 * @property duration Total workflow execution duration.
 * @property error Workflow execution failure, if any.
 */
data class ExecutionResult<T>(
    val executionId: String,
    val workflowName: String,
    val status: ExecutionStatus,
    val value: T?,
    val steps: List<StepExecution>,
    val duration: Duration,
    val error: Throwable? = null
) {

    /**
     * Whether the workflow completed successfully.
     */
    val successful: Boolean
        get() = status == ExecutionStatus.SUCCESS

    /**
     * Whether the workflow execution failed.
     */
    val isFailure: Boolean
        get() = status == ExecutionStatus.FAILED

    /**
     * Returns the workflow output or throws the execution failure.
     *
     * @return Workflow output.
     * @throws Throwable If the workflow execution failed.
     */
    fun getOrThrow(): T {
        if (isFailure) {
            throw error ?: IllegalStateException(
                "Workflow execution failed: $workflowName"
            )
        }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    /**
     * Returns the workflow output when execution succeeds.
     *
     * @return Workflow output, or null when execution failed.
     */
    fun getOrNull(): T? =
        if (successful) value else null

    /**
     * Returns the execution failure, if one occurred.
     *
     * @return Execution exception, or null on success.
     */
    fun exceptionOrNull(): Throwable? =
        if (isFailure) error else null

    /**
     * Transforms the execution outcome into another value.
     *
     * @param R Result type of the transformation.
     * @param onSuccess Transformation applied to successful output.
     * @param onFailure Transformation applied to execution failure.
     * @return Value produced by the corresponding transformation.
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Throwable?) -> R
    ): R = when (status) {
        ExecutionStatus.SUCCESS -> onSuccess(getOrThrow())
        ExecutionStatus.FAILED -> onFailure(error)
    }
}
