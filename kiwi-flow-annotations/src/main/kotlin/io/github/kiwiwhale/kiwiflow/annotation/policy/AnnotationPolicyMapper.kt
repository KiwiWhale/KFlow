package io.github.kiwiwhale.kiwiflow.annotation.policy

import io.github.kiwiwhale.kiwiflow.annotation.Retry
import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import kotlin.time.Duration.Companion.milliseconds

internal object AnnotationPolicyMapper {

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
