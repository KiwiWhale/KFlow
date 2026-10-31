package io.github.mrjimin.kflow.core.policy

import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

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

    fun delayFor(attempt: Int): Duration {
        require(attempt >= 1) {
            "attempt must be at least 1"
        }

        if (initialDelay == Duration.ZERO) {
            return Duration.ZERO
        }

        var baseDelay = initialDelay

        if (multiplier > 1.0) {
            var remaining = attempt - 1

            while (remaining > 0 && baseDelay < maxDelay) {
                baseDelay = (baseDelay * multiplier)
                    .coerceAtMost(maxDelay)

                remaining--
            }
        }

        val jitteredDelay = when (jitter) {
            RetryJitter.NONE -> baseDelay

            RetryJitter.FULL ->
                baseDelay * Random.nextDouble()

            RetryJitter.EQUAL ->
                baseDelay * (0.5 + Random.nextDouble() * 0.5)
        }

        return jitteredDelay.coerceAtMost(maxDelay)
    }

    internal fun shouldRetry(exception: Exception): Boolean =
        condition.shouldRetry(exception)
}
