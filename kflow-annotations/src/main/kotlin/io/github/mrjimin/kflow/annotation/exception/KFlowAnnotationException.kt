package io.github.mrjimin.kflow.annotation.exception

import io.github.mrjimin.kflow.exception.KFlowException

open class KFlowAnnotationException(
    message: String,
    cause: Throwable? = null
) : KFlowException(message, cause)