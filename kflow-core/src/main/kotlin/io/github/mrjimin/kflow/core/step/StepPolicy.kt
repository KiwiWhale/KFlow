package io.github.mrjimin.kflow.core.step

import io.github.mrjimin.kflow.core.policy.RetryPolicy
import kotlin.time.Duration

internal data class StepPolicy(
    val retry: RetryPolicy? = null,
    val timeout: Duration? = null
)