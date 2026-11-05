package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.time.Duration

/**
 * Immutable metadata describing an annotated workflow step.
 */
internal class StepMetadata(
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

        val actualInput = function.valueParameters.singleOrNull()

        require(inputParameter == actualInput) {
            "Step input metadata does not match its function."
        }

        require(
            timeout == null ||
                    (timeout.isFinite() && timeout.isPositive())
        ) {
            "Step timeout must be finite and positive."
        }
    }
}
