package io.github.kiwiwhale.kiwiflow.annotation.exception

import io.github.kiwiwhale.kiwiflow.annotation.Workflow

/**
 * Thrown when a workflow declaration is invalid.
 *
 * This exception is raised when a workflow cannot be resolved or
 * does not satisfy the requirements for annotation-based discovery.
 *
 * Examples include missing step declarations, ambiguous workflow
 * annotations, and duplicate step names.
 *
 * @property workflowName Resolved name of the invalid workflow.
 * @param message Description of the validation failure.
 * @param cause Underlying cause of the failure, if available.
 *
 * @see Workflow
 * @see InvalidStepException
 */
class InvalidWorkflowException(
    val workflowName: String,
    message: String,
    cause: Throwable? = null
) : KiwiFlowAnnotationException(
    message = "Invalid KiwiFlow workflow '$workflowName': $message",
    cause = cause
)