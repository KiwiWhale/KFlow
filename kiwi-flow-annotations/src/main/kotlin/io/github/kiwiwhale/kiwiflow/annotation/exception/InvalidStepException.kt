package io.github.kiwiwhale.kiwiflow.annotation.exception

/**
 * Thrown when a step declaration is invalid.
 *
 * This exception is raised when a method annotated with [Step]
 * violates the supported step declaration rules or when its
 * metadata cannot be inspected successfully.
 *
 * @property stepName Name of the invalid step.
 * @param message Description of the validation failure.
 * @param cause The underlying cause of the failure, if available.
 *
 * @see Step
 */
class InvalidStepException(
    val stepName: String,
    message: String,
    cause: Throwable? = null
) : KiwiFlowAnnotationException(
    message = "Invalid KiwiFlow step '$stepName': $message",
    cause = cause
)