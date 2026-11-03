package io.github.kiwiwhale.kiwiflow.annotation.exception

/**
 * Thrown when a workflow declaration is invalid.
 *
 * This exception is raised when a workflow does not satisfy the
 * requirements for annotation-based workflow discovery, such as
 * missing step declarations or duplicate step names.
 *
 * @property workflowName Name of the invalid workflow.
 * @param message Description of the validation failure.
 * @param cause The underlying cause of the failure, if available.
 *
 * @see Workflow
 */
class InvalidWorkflowException(
    val workflowName: String,
    message: String,
    cause: Throwable? = null
) : KiwiFlowAnnotationException(
    message = "Invalid KiwiFlow workflow '$workflowName': $message",
    cause = cause
)