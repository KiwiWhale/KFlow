package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.time.Duration

/**
 * Represents the resolved metadata of an annotated workflow step.
 *
 * Stores the reflected Kotlin function and its execution configuration.
 * This metadata is created during workflow inspection and consumed
 * by the workflow factory and step invoker.
 *
 * A step can accept either no input parameter or exactly one input
 * parameter.
 *
 * @property function Reflected Kotlin function representing the step.
 * @property name Resolved step name.
 * @property order Execution order used when sorting workflow steps.
 * @property timeout Maximum execution duration, or null if disabled.
 * @property retryPolicy Retry configuration, or null if retries are disabled.
 * @property inputParameter The step input parameter, or null if the
 * step does not accept an input.
 *
 * @see StepInvoker
 * @see WorkflowMetadata
 */
internal data class StepMetadata(
    val function: KFunction<*>,
    val name: String,
    val order: Int,
    val timeout: Duration?,
    val retryPolicy: RetryPolicy?,
    val inputParameter: KParameter?
) {

    init {
        require(name.isNotBlank()) {
            "Step name cannot be blank."
        }

        require(timeout == null || (timeout.isFinite() && timeout.isPositive())) {
            "Step timeout must be finite and positive."
        }

        require(
            inputParameter == null ||
                    inputParameter.kind == KParameter.Kind.VALUE
        ) {
            "Step input parameter must be a value parameter."
        }
    }
}
