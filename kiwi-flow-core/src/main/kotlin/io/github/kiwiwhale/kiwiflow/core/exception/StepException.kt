package io.github.kiwiwhale.kiwiflow.core.exception

class StepException(
    val stepName: String,
    val attempt: Int,
    message: String,
    cause: Throwable? = null
) : KiwiFlowException(
    message = "Step '$stepName' failed at attempt $attempt: $message",
    cause = cause
)