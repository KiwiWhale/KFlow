package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidStepException
import io.github.kiwiwhale.kiwiflow.annotation.exception.KiwiFlowAnnotationException
import io.github.kiwiwhale.kiwiflow.annotation.metadata.StepMetadata
import io.github.kiwiwhale.kiwiflow.annotation.policy.AnnotationPolicyMapper
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.time.Duration.Companion.milliseconds

/**
 * Discovers and resolves workflow step declarations.
 *
 * Direct and composed Step annotations are supported.
 * Function declarations are validated before metadata creation.
 */
internal class StepInspector {

    /**
     * Inspects a function for a workflow step declaration.
     *
     * @param function Function to inspect.
     * @return Step metadata, or null if no Step annotation exists.
     */
    fun inspect(function: KFunction<*>): StepMetadata? {
        val annotation = resolveAnnotation(function)
            ?: return null

        val name = annotation.name.ifBlank {
            function.name
        }

        try {
            ReflectionValidator.validateStep(
                function = function,
                annotation = annotation
            )

            val inputParameter = function.parameters
                .singleOrNull {
                    it.kind == KParameter.Kind.VALUE
                }

            val timeout = annotation.timeoutMillis
                .takeIf { it > 0L }
                ?.milliseconds

            return StepMetadata(
                function = function,
                name = name,
                order = annotation.order,
                timeout = timeout,
                retryPolicy = AnnotationPolicyMapper.toRetryPolicy(
                    annotation.retry
                ),
                inputParameter = inputParameter
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

    private fun resolveAnnotation(
        function: KFunction<*>
    ): Step? =
        try {
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
        }
}
