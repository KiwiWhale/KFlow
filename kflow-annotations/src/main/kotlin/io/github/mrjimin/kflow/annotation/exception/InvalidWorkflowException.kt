package io.github.mrjimin.kflow.annotation.exception

class InvalidWorkflowException(
    val workflowName: String,
    message: String,
    cause: Throwable? = null
) : KFlowAnnotationException(
    message = "Invalid KFlow workflow '$workflowName': $message",
    cause = cause
)