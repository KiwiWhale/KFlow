package io.github.kiwiwhale.kiwiflow.annotation.exception

class InvalidWorkflowException(
    val workflowName: String,
    message: String,
    cause: Throwable? = null
) : KiwiFlowAnnotationException(
    message = "Invalid KiwiFlow workflow '$workflowName': $message",
    cause = cause
)