package io.github.kiwiwhale.kiwiflow.core.exception

/**
 * Indicates that a workflow step execution has failed.
 *
 * Contains the name of the failed step and the attempt number
 * at which the failure occurred.
 *
 * @property stepName Name of the failed step.
 * @property attempt Execution attempt number.
 * @param message Description of the failure.
 * @param cause Original failure cause.
 */
class StepException(
    val stepName: String,
    val attempt: Int,
    message: String,
    cause: Throwable? = null
) : KiwiFlowException(
    message = "Step '$stepName' failed at attempt $attempt: $message",
    cause = cause
)