package io.github.mrjimin.kflow.step

import io.github.mrjimin.kflow.policy.RetryPolicy
import kotlin.time.Duration

internal data class StepPolicy(
    val retry: RetryPolicy? = null,
    val timeout: Duration? = null
)