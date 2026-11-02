package io.github.kiwiwhale.kiwiflow.core.step

import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import kotlin.time.Duration

internal data class StepPolicy(
    val retry: RetryPolicy? = null,
    val timeout: Duration? = null
)