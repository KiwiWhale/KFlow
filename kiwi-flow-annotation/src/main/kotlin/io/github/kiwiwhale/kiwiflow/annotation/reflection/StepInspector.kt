package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidStepException
import io.github.kiwiwhale.kiwiflow.annotation.exception.KiwiFlowAnnotationException
import io.github.kiwiwhale.kiwiflow.annotation.metadata.StepMetadata
import io.github.kiwiwhale.kiwiflow.annotation.policy.AnnotationPolicyMapper
import kotlin.reflect.KFunction
import kotlin.time.Duration.Companion.milliseconds

/**
 * Discovers and inspects workflow step functions.
 *
 * Resolves direct and composed [Step] annotations, validates
 * their declarations, and converts them into [StepMetadata].
 */
internal class StepInspector {

    /**
     * Inspects a function for a [Step] annotation.
     *
     * Returns null when the function is not a workflow step.
     *
     * @param function Function to inspect.
     * @return Resolved step metadata, or null if no step annotation
     * is present.
     * @throws InvalidStepException If the step declaration is invalid.
     */
    fun inspect(function: KFunction<*>): StepMetadata? {
        val annotation = try {
            AnnotationResolver.find(
                annotations = function.annotations.toTypedArray(),
                target = Step::class
            )
        } catch (exception: IllegalArgumentException) {
            throw InvalidStepException(
                stepName = function.name,
                message = "Multiple @Step annotations were found.",
                cause = exception
            )
        } ?: return null

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
