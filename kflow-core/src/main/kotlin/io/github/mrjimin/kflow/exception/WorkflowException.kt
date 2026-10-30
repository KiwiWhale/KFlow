package io.github.mrjimin.kflow.exception

class WorkflowException(
    val workflowName: String,
    message: String,
    cause: Throwable? = null
) : KFlowException(
    message = "Workflow '$workflowName': $message",
    cause = cause
)