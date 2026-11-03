package io.github.kiwiwhale.kiwiflow.annotation.exception

import io.github.kiwiwhale.kiwiflow.core.exception.KiwiFlowException

/**
 * Base exception for errors related to KiwiFlow annotation processing.
 *
 * This exception represents failures that occur while discovering,
 * validating, or interpreting workflow and step annotations.
 *
 * @param message Description of the annotation processing failure.
 * @param cause The underlying cause of the failure, if available.
 */
open class KiwiFlowAnnotationException(
    message: String,
    cause: Throwable? = null
) : KiwiFlowException(message, cause)