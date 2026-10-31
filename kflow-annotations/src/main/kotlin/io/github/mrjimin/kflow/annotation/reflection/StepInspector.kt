package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.exception.InvalidStepException
import io.github.mrjimin.kflow.annotation.exception.KFlowAnnotationException
import io.github.mrjimin.kflow.annotation.metadata.StepMetadata
import io.github.mrjimin.kflow.annotation.policy.AnnotationPolicyMapper
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.time.Duration.Companion.milliseconds

internal class StepInspector {

    fun inspect(function: KFunction<*>): StepMetadata? {
        val annotation = function.findAnnotation<Step>()
            ?: return null

        val name = annotation.name.ifBlank {
            function.name
        }

        try {
            ReflectionValidator.validateStep(
                function = function,
                annotation = annotation
            )

            val timeout = annotation.timeoutMillis
                .takeIf { it > 0L }
                ?.milliseconds

            if (timeout != null && !timeout.isFinite()) {
                throw InvalidStepException(
                    name,
                    "timeoutMillis exceeds the supported duration range."
                )
            }

            val retryPolicy = AnnotationPolicyMapper.toRetryPolicy(
                annotation.retry
            )

            return StepMetadata(
                function = function,
                name = name,
                order = annotation.order,
                timeout = timeout,
                retryPolicy = retryPolicy,
                inputParameter = function.parameters.getOrNull(1)
            )

        } catch (exception: KFlowAnnotationException) {
            throw exception

        } catch (exception: IllegalArgumentException) {
            throw InvalidStepException(
                stepName = name,
                message = "Invalid step configuration.",
                cause = exception
            )

        } catch (exception: Exception) {
            throw InvalidStepException(
                stepName = name,
                message = "Failed to inspect step method.",
                cause = exception
            )
        }
    }
}