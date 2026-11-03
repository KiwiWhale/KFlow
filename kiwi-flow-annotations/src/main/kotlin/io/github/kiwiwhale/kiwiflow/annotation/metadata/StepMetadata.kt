package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.core.policy.RetryPolicy
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.time.Duration

/**
 * Contains the resolved metadata of an annotated workflow step.
 *
 * This metadata is generated during step inspection and is used
 * by the workflow factory and step invoker.
 *
 * @property function Reflected Kotlin function representing the step.
 * @property name Resolved step name.
 * @property order Execution order of the step.
 * @property timeout Maximum execution duration, or null if disabled.
 * @property retryPolicy Resolved retry policy, or null if retries are disabled.
 * @property inputParameter The step input parameter, or null for
 * parameterless steps.
 */
internal data class StepMetadata(
    val function: KFunction<*>,
    val name: String,
    val order: Int,
    val timeout: Duration?,
    val retryPolicy: RetryPolicy?,
    val inputParameter: KParameter?
)