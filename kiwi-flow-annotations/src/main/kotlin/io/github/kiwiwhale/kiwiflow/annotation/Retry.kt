package io.github.kiwiwhale.kiwiflow.annotation

import io.github.kiwiwhale.kiwiflow.core.policy.RetryJitter

/**
 * Configures the retry policy for a KiwiFlow step.
 *
 * Failed step executions may be retried according to the configured
 * exponential backoff strategy.
 *
 * When [maxAttempts] is one, retries are disabled.
 *
 * This annotation can be used directly through [Step] or as a
 * meta-annotation on custom retry configurations.
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