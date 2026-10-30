package io.github.mrjimin.kflow.policy

fun interface RetryCondition {

    fun shouldRetry(exception: Exception): Boolean

    companion object {
        val ALWAYS = RetryCondition { true }
        val NEVER = RetryCondition { false }
    }
}
