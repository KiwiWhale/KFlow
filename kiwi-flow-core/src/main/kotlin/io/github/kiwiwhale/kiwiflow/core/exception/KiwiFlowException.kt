package io.github.kiwiwhale.kiwiflow.core.exception

open class KiwiFlowException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)