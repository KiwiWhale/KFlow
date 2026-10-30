package io.github.mrjimin.kflow.exception

open class KFlowException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)