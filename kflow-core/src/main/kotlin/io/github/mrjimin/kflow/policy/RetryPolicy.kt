package io.github.mrjimin.kflow.policy

import kotlin.math.pow
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
        require(maxAttempts >= 1)
        require(initialDelay >= Duration.ZERO)
        require(maxDelay >= initialDelay)
        require(multiplier.isFinite() && multiplier >= 1.0)
    }

    internal fun delayFor(attempt: Int): Duration {
        require(attempt >= 1)

        if (initialDelay == Duration.ZERO) {
            return Duration.ZERO
        }

        val factor = multiplier.pow(attempt - 1)

        val baseDelay = if (!factor.isFinite()) {
            maxDelay
        } else {
            (initialDelay * factor).coerceAtMost(maxDelay)
        }

        return when (jitter) {
            RetryJitter.NONE -> baseDelay

            RetryJitter.FULL ->
                baseDelay * Random.nextDouble()

            RetryJitter.EQUAL ->
                baseDelay * (
                        0.5 + Random.nextDouble() * 0.5
                        )
        }.coerceAtMost(maxDelay)
    }

    internal fun shouldRetry(exception: Exception): Boolean =
        condition.shouldRetry(exception)
}
