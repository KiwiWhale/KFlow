package io.github.kiwiwhale.kiwiflow.core.policy

import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Defines the retry behavior for a workflow step.
 *
 * Supports exponential backoff, maximum delay limits,
 * configurable jitter, and conditional retry decisions.
 *
 * The maximum number of attempts includes the initial execution.
 *
 * Example:
 *
 * ```
 * val policy = RetryPolicy(
 *     maxAttempts = 3,
 *     initialDelay = 200.milliseconds,
 *     multiplier = 2.0,
 *     maxDelay = 5.seconds,
 *     jitter = RetryJitter.FULL
 * )
 * ```
 *
 * @property maxAttempts Maximum number of execution attempts.
 * @property initialDelay Delay before the first retry.
 * @property multiplier Exponential backoff multiplier.
 * @property maxDelay Maximum permitted retry delay.
 * @property jitter Retry delay randomization strategy.
 * @property condition Determines whether an exception is retryable.
 */
data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelay: Duration = 100.milliseconds,
    val multiplier: Double = 2.0,
    val maxDelay: Duration = 10_000.milliseconds,
    val jitter: RetryJitter = RetryJitter.NONE,
    val condition: RetryCondition = RetryCondition.ALWAYS
) {

    init {
        require(maxAttempts >= 1) {
            "maxAttempts must be at least 1"
        }

        require(initialDelay.isFinite() && initialDelay >= Duration.ZERO) {
            "initialDelay must be finite and non-negative"
        }

        require(maxDelay.isFinite() && maxDelay >= initialDelay) {
            "maxDelay must be finite and greater than or equal to initialDelay"
        }

        require(multiplier.isFinite() && multiplier >= 1.0) {
            "multiplier must be finite and greater than or equal to 1.0"
        }
    }

    /**
     * Calculates the retry delay for the specified failed attempt.
     *
     * The delay follows exponential backoff and is capped at
     * [maxDelay]. The configured jitter strategy is applied afterward.
     *
     * Attempt numbering starts at one, where attempt one represents
     * the initial execution.
     *
     * @param attempt Failed execution attempt.
     * @return Calculated delay before the next attempt.
     * @throws IllegalArgumentException If attempt is less than one.
     */
    fun delayFor(attempt: Int): Duration {
        require(attempt >= 1) {
            "attempt must be at least 1"
        }

        if (initialDelay == Duration.ZERO) {
            return Duration.ZERO
        }

        var baseDelay = initialDelay
        var remaining = attempt - 1

        while (remaining > 0 && baseDelay < maxDelay) {
            baseDelay = (baseDelay * multiplier)
                .coerceAtMost(maxDelay)

            remaining--
        }

        val jitteredDelay = when (jitter) {
            RetryJitter.NONE -> baseDelay
            RetryJitter.FULL -> baseDelay * Random.nextDouble()
            RetryJitter.EQUAL ->
                baseDelay * (0.5 + Random.nextDouble() * 0.5)
        }

        return jitteredDelay.coerceAtMost(maxDelay)
    }

    /**
     * Determines whether the supplied exception should trigger a retry.
     *
     * @param exception Failure from the current attempt.
     * @return True when the configured retry condition accepts the failure.
     */
    internal fun shouldRetry(exception: Exception): Boolean =
        condition.shouldRetry(exception)
}
