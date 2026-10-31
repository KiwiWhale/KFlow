package io.github.mrjimin.kflow.annotation.metadata

import io.github.mrjimin.kflow.policy.RetryPolicy
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.time.Duration

internal data class StepMetadata(
    val function: KFunction<*>,
    val name: String,
    val order: Int,
    val timeout: Duration?,
    val retryPolicy: RetryPolicy?,
    val inputParameter: KParameter?
)