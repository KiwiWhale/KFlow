package io.github.mrjimin.kflow.exception

import kotlin.time.Duration

class StepTimeoutException(
    val stepName: String,
    val timeout: Duration,
    cause: Throwable? = null
) : KFlowException(
    message = "Step '$stepName' timed out after $timeout",
    cause = cause
)