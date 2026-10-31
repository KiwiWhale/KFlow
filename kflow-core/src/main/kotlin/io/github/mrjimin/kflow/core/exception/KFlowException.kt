package io.github.mrjimin.kflow.core.exception

open class KFlowException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)