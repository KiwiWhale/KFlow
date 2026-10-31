package io.github.mrjimin.kflow.core.exception

class RetryExhaustedException(
    val stepName: String,
    val maxAttempts: Int,
    cause: Throwable? = null
) : KFlowException(
    message = "Step '$stepName' exhausted all $maxAttempts attempts",
    cause = cause
)