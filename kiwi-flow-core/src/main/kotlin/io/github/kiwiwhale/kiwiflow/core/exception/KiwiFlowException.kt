package io.github.kiwiwhale.kiwiflow.core.exception

/**
 * Base exception for all KiwiFlow runtime errors.
 *
 * Provides a common exception type for workflow execution,
 * step execution, timeout, and retry failures.
 *
 * @param message Description of the failure.
 * @param cause Original cause of the failure, if available.
 */
open class KiwiFlowException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)