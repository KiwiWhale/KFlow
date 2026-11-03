package io.github.kiwiwhale.kiwiflow.core.policy

/**
 * Defines the randomization strategy applied to retry delays.
 *
 * Jitter helps distribute retry attempts and reduce synchronized
 * retry traffic when multiple executions fail simultaneously.
 */
enum class RetryJitter {

    /** Uses the calculated exponential backoff delay without randomization. */
    NONE,

    /** Selects a random delay between zero and the calculated delay. */
    FULL,

    /** Selects a random delay between 50% and 100% of the calculated delay. */
    EQUAL
}
