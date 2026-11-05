package io.github.kiwiwhale.kiwiflow.annotation

import io.github.kiwiwhale.kiwiflow.core.policy.RetryJitter

/**
 * Defines retry configuration for a KiwiFlow step.
 *
 * Configures the retry behavior applied when a step execution fails.
 *
 * Retry delays follow an exponential backoff strategy, optionally
 * applying a jitter strategy to reduce synchronized retries.
 *
 * A maximum attempt count of one disables retries.
 *
 * This annotation is intended for use as part of [Step]
 * configuration.
 *
 * Example:
 *
 * ```
 * @Step(
 *     retry = Retry(
 *         maxAttempts = 3,
 *         initialDelayMillis = 200,
 *         maxDelayMillis = 5_000,
 *         multiplier = 2.0,
 *         jitter = RetryJitter.FULL
 *     )
 * )
 * fun process(input: String): String = input
 * ```
 *
 * @property maxAttempts Maximum number of execution attempts,
 * including the initial execution.
 * @property initialDelayMillis Initial delay before the first retry.
 * @property maxDelayMillis Maximum delay between retry attempts.
 * @property multiplier Exponential backoff multiplier.
 * @property jitter Jitter strategy applied to retry delays.
 *
 * @see Step
 * @see RetryJitter
 */
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
