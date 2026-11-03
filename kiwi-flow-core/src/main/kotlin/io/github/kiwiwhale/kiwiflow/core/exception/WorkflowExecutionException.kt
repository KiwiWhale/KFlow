package io.github.kiwiwhale.kiwiflow.core.exception

/**
 * Indicates that a workflow execution has failed.
 *
 * Wraps the original execution failure and associates it with
 * a unique execution identifier.
 *
 * @property executionId Unique identifier of the failed execution.
 * @param workflowName Name of the failed workflow.
 * @param cause Original execution failure.
 */
class WorkflowExecutionException(
    val executionId: String,
    workflowName: String,
    cause: Throwable
) : WorkflowException(
    workflowName = workflowName,
    message = "Execution '$executionId' failed: ${cause.message ?: cause.javaClass.simpleName}",
    cause = cause
) {

    init {
        require(executionId.isNotBlank()) {
            "Execution ID cannot be blank"
        }
    }
}
