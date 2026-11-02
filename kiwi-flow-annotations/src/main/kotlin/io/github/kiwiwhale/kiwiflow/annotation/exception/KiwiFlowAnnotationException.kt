package io.github.kiwiwhale.kiwiflow.annotation.exception

import io.github.kiwiwhale.kiwiflow.core.exception.KiwiFlowException

open class KiwiFlowAnnotationException(
    message: String,
    cause: Throwable? = null
) : KiwiFlowException(message, cause)