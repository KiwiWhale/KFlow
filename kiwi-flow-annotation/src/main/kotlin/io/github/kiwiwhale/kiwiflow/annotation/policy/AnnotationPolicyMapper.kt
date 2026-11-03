package io.github.kiwiwhale.kiwiflow.annotation.policy

import io.github.kiwiwhale.kiwiflow.annotation.Retry
import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import kotlin.time.Duration.Companion.milliseconds

/**
 * Converts annotation-based retry configuration into core retry policies.
 *
 * Keeps annotation processing independent from the core retry policy
 * implementation.
 */
internal object AnnotationPolicyMapper {

    /**
     * Converts a [Retry] annotation into a [RetryPolicy].
     *
     * Returns null when retries are disabled.
     *
     * @param annotation Retry configuration.
     * @return Corresponding retry policy, or null when maxAttempts is one.
     */
    fun toRetryPolicy(annotation: Retry): RetryPolicy? {
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
