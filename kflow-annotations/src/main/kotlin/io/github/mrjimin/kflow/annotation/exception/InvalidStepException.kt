package io.github.mrjimin.kflow.annotation.exception

class InvalidStepException(
    val stepName: String,
    message: String,
    cause: Throwable? = null
) : KFlowAnnotationException(
    message = "Invalid KFlow step '$stepName': $message",
    cause = cause
)