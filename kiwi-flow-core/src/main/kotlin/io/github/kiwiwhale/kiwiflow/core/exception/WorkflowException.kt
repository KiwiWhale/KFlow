package io.github.kiwiwhale.kiwiflow.core.exception

class WorkflowException(
    val workflowName: String,
    message: String,
    cause: Throwable? = null
) : KiwiFlowException(
    message = "Workflow '$workflowName': $message",
    cause = cause
)