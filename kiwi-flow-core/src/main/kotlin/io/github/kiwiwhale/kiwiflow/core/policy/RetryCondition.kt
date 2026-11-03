package io.github.kiwiwhale.kiwiflow.core.policy

/**
 * Determines whether a failed step execution should be retried.
 *
 * Implementations receive the exception raised by the failed attempt
 * and decide whether another attempt should be performed.
 *
 * @see RetryPolicy
 */
fun interface RetryCondition {

    /**
     * Evaluates whether the specified exception is retryable.
     *
     * @param exception Exception raised during step execution.
     * @return True if another attempt should be performed.
     */
    fun shouldRetry(exception: Exception): Boolean

    companion object {

        /** Retries every eligible execution failure. */
        val ALWAYS = RetryCondition { true }

        /** Never retries execution failures. */
        val NEVER = RetryCondition { false }
    }
}
