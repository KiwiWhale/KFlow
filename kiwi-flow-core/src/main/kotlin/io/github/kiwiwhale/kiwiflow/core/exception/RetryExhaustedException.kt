package io.github.kiwiwhale.kiwiflow.core.exception

class RetryExhaustedException(
    val stepName: String,
    val maxAttempts: Int,
    cause: Throwable? = null
) : KiwiFlowException(
    message = "Step '$stepName' exhausted all $maxAttempts attempts",
    cause = cause
)