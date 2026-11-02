package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidStepException
import io.github.kiwiwhale.kiwiflow.annotation.exception.KiwiFlowAnnotationException
import io.github.kiwiwhale.kiwiflow.annotation.metadata.StepMetadata
import io.github.kiwiwhale.kiwiflow.annotation.policy.AnnotationPolicyMapper
import kotlin.reflect.KFunction
import kotlin.time.Duration.Companion.milliseconds

internal class StepInspector {

    fun inspect(function: KFunction<*>): StepMetadata? {
        val annotation = AnnotationResolver.find(
            annotations = function.annotations.toTypedArray(),
            target = Step::class
        ) ?: return null

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

            return StepMetadata(
                function = function,
                name = name,
                order = annotation.order,
                timeout = timeout,
                retryPolicy = AnnotationPolicyMapper.toRetryPolicy(
                    annotation.retry
                ),
                inputParameter = function.parameters.getOrNull(1)
            )

        } catch (exception: KiwiFlowAnnotationException) {
            throw exception

        } catch (exception: Exception) {
            throw InvalidStepException(
                stepName = name,
                message = "Failed to inspect step method.",
                cause = exception
            )
        }
    }
}
