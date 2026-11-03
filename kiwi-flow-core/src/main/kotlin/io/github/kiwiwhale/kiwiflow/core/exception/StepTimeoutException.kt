package io.github.kiwiwhale.kiwiflow.core.exception

import kotlin.time.Duration

/**
 * Indicates that a workflow step exceeded its configured timeout.
 *
 * @property stepName Name of the timed-out step.
 * @property timeout Configured execution timeout.
 * @param cause Original timeout cancellation cause, if available.
 */
class StepTimeoutException(
    val stepName: String,
    val timeout: Duration,
    cause: Throwable? = null
) : KiwiFlowException(
    message = "Step '$stepName' timed out after $timeout",
    cause = cause
)