package io.github.kiwiwhale.kiwiflow.core.policy

fun interface RetryCondition {

    fun shouldRetry(exception: Exception): Boolean

    companion object {
        val ALWAYS = RetryCondition { true }
        val NEVER = RetryCondition { false }
    }
}
