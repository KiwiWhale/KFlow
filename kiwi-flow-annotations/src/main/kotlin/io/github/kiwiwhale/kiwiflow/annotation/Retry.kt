package io.github.kiwiwhale.kiwiflow.annotation

import io.github.kiwiwhale.kiwiflow.core.policy.RetryJitter

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KiwiFlowAnnotation
annotation class Retry(
    val maxAttempts: Int = 1,
    val initialDelayMillis: Long = 100L,
    val maxDelayMillis: Long = 10_000L,
    val multiplier: Double = 2.0,
    val jitter: RetryJitter = RetryJitter.NONE
)