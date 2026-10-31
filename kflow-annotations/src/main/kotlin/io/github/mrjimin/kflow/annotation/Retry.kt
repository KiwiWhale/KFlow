package io.github.mrjimin.kflow.annotation

import io.github.mrjimin.kflow.core.policy.RetryJitter

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Retry(
    val maxAttempts: Int = 1,
    val initialDelayMillis: Long = 100L,
    val maxDelayMillis: Long = 10_000L,
    val multiplier: Double = 2.0,
    val jitter: RetryJitter = RetryJitter.NONE
)