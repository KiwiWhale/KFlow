package io.github.mrjimin.kflow.annotation

import io.github.mrjimin.kflow.policy.RetryJitter

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class StepRetry(
    val maxAttempts: Int = 3,
    val initialDelayMillis: Long = 100L,
    val maxDelayMillis: Long = 10_000L,
    val multiplier: Double = 2.0,
    val jitter: RetryJitter = RetryJitter.NONE
)