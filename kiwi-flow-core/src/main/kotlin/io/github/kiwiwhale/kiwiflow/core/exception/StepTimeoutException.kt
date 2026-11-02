package io.github.kiwiwhale.kiwiflow.core.exception

import kotlin.time.Duration

class StepTimeoutException(
    val stepName: String,
    val timeout: Duration,
    cause: Throwable? = null
) : KiwiFlowException(
    message = "Step '$stepName' timed out after $timeout",
    cause = cause
)