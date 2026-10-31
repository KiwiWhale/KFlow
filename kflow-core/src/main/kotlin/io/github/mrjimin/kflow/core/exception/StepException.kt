package io.github.mrjimin.kflow.core.exception

class StepException(
    val stepName: String,
    val attempt: Int,
    message: String,
    cause: Throwable? = null
) : KFlowException(
    message = "Step '$stepName' failed at attempt $attempt: $message",
    cause = cause
)