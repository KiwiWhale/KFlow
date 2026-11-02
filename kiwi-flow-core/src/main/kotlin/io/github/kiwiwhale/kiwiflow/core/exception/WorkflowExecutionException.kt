package io.github.kiwiwhale.kiwiflow.core.exception

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
