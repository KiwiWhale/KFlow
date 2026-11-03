package io.github.kiwiwhale.kiwiflow.core.step

import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import kotlin.time.Duration

/**
 * Contains execution policies applied to a workflow step.
 *
 * @property retry Retry configuration, or null when retries are disabled.
 * @property timeout Execution timeout, or null when no timeout is configured.
 */
internal data class StepPolicy(
    val retry: RetryPolicy? = null,
    val timeout: Duration? = null
)