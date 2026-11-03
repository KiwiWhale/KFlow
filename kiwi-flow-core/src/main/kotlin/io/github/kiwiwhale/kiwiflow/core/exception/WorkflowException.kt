package io.github.kiwiwhale.kiwiflow.core.exception

/**
 * Base exception for workflow-related failures.
 *
 * Contains the name of the workflow associated with the failure.
 *
 * @property workflowName Name of the affected workflow.
 * @param message Description of the failure.
 * @param cause Original cause of the failure, if available.
 */
open class WorkflowException(
    val workflowName: String,
    message: String,
    cause: Throwable? = null
) : KiwiFlowException(
    message = "Workflow '$workflowName': $message",
    cause = cause
)