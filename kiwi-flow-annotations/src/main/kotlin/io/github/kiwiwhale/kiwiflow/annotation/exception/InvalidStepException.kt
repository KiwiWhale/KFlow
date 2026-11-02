package io.github.kiwiwhale.kiwiflow.annotation.exception

class InvalidStepException(
    val stepName: String,
    message: String,
    cause: Throwable? = null
) : KiwiFlowAnnotationException(
    message = "Invalid KiwiFlow step '$stepName': $message",
    cause = cause
)