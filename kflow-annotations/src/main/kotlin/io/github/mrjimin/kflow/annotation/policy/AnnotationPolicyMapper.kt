package io.github.mrjimin.kflow.annotation.policy

import io.github.mrjimin.kflow.annotation.StepRetry
import io.github.mrjimin.kflow.policy.RetryPolicy
import kotlin.time.Duration.Companion.milliseconds

internal object AnnotationPolicyMapper {

    fun toRetryPolicy(annotation: StepRetry): RetryPolicy? {
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
