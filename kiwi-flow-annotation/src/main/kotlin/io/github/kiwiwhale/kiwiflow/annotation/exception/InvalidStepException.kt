package io.github.kiwiwhale.kiwiflow.annotation.exception

import io.github.kiwiwhale.kiwiflow.annotation.Step

/**
 * Thrown when a workflow step declaration is invalid.
 *
 * This exception is raised when a function annotated with [Step]
 * violates the supported declaration rules or when its metadata
 * cannot be resolved successfully.
 *
 * @property stepName Resolved name of the invalid step.
 * @param message Description of the validation failure.
 * @param cause Underlying cause of the failure, if available.
 *
 * @see Step
 * @see InvalidWorkflowException
 */
class InvalidStepException(
    val stepName: String,
    message: String,
    cause: Throwable? = null
) : KiwiFlowAnnotationException(
    message = "Invalid KiwiFlow step '$stepName': $message",
    cause = cause
)
