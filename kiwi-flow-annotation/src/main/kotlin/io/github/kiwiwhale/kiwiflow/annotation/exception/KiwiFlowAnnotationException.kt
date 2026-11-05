package io.github.kiwiwhale.kiwiflow.annotation.exception

import io.github.kiwiwhale.kiwiflow.core.exception.KiwiFlowException

/**
 * Base exception for failures during KiwiFlow annotation processing.
 *
 * This exception represents errors encountered while discovering,
 * resolving, validating, or interpreting workflow and step annotations.
 *
 * More specific exceptions should be used when the failure concerns
 * an individual workflow or step declaration.
 *
 * @param message Description of the annotation processing failure.
 * @param cause Underlying cause of the failure, if available.
 *
 * @see InvalidWorkflowException
 * @see InvalidStepException
 */
open class KiwiFlowAnnotationException(
    message: String,
    cause: Throwable? = null
) : KiwiFlowException(message, cause)