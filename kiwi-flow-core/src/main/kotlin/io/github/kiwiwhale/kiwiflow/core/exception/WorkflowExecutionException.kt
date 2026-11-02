package io.github.kiwiwhale.kiwiflow.core.exception

class WorkflowExecutionException(
    val executionId: String,
    val workflowName: String,
    cause: Throwable
) : KiwiFlowException(
    message = "Execution '$executionId' of workflow '$workflowName' failed: ${cause.message ?: cause.javaClass.simpleName}",
    cause = cause
) {

    init {
        require(executionId.isNotBlank()) {
            "Execution ID cannot be blank"
        }

        require(workflowName.isNotBlank()) {
            "Workflow name cannot be blank"
        }
    }
}
