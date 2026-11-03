package io.github.kiwiwhale.kiwiflow.core.exception

/**
 * Indicates that a step has exhausted its configured execution attempts.
 *
 * This exception is raised when the retry condition accepts a failure
 * but no further attempts are available.
 *
 * @property stepName Name of the affected step.
 * @property maxAttempts Maximum number of allowed attempts.
 * @param cause Failure from the final attempt.
 */
class RetryExhaustedException(
    val stepName: String,
    val maxAttempts: Int,
    cause: Throwable? = null
) : KiwiFlowException(
    message = "Step '$stepName' exhausted all $maxAttempts attempts",
    cause = cause
)