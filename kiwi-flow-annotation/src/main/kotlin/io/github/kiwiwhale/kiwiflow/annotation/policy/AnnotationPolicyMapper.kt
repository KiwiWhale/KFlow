package io.github.kiwiwhale.kiwiflow.annotation.policy

import io.github.kiwiwhale.kiwiflow.annotation.Retry
import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import kotlin.time.Duration.Companion.milliseconds

/**
 * Maps annotation-based retry configuration to Core retry policies.
 *
 * Keeps annotation processing independent of retry policy
 * construction details.
 */
internal object AnnotationPolicyMapper {

    /**
     * Converts a [Retry] annotation into a [RetryPolicy].
     *
     * @param annotation Retry configuration.
     * @return Corresponding retry policy, or null when retries
     * are disabled.
     */
    fun toRetryPolicy(
        annotation: Retry
    ): RetryPolicy? {
        if (annotation.maxAttempts == 1) {
            return null
        }

        return RetryPolicy(
            maxAttempts = annotation.maxAttempts,
            initialDelay = annotation.initialDelayMillis.milliseconds,
            maxDelay = annotation.maxDelayMillis.milliseconds,
            multiplier = annotation.multiplier,
            jitter = annotation.jitter
        )
    }
}
