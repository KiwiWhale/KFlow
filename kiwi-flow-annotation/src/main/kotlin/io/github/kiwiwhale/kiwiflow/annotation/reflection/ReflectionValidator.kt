package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidStepException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.time.Duration.Companion.milliseconds

/**
 * Validates Kotlin function declarations used as workflow steps.
 *
 * Supported functions:
 * - Public instance member functions.
 * - Regular and suspend functions.
 * - Zero or one non-optional value parameter.
 * - Non-generic functions with resolvable runtime types.
 *
 * Unsupported declarations are rejected before invocation.
 */
internal object ReflectionValidator {

    fun validateStep(
        function: KFunction<*>,
        annotation: Step
    ) {
        val name = annotation.name.ifBlank {
            function.name
        }

        fun invalid(message: String): Nothing =
            throw InvalidStepException(name, message)

        validateVisibility(function, ::invalid)
        validateReceiver(function, ::invalid)
        validateParameters(function, ::invalid)
        validateFunctionType(function, ::invalid)
        validateTimeout(annotation.timeoutMillis, ::invalid)
        validateRetry(annotation, ::invalid)
    }

    private fun validateVisibility(
        function: KFunction<*>,
        invalid: (String) -> Nothing
    ) {
        if (function.visibility != KVisibility.PUBLIC) {
            invalid("Step method must be public.")
        }

        if (function.isAbstract) {
            invalid("Abstract step methods are not supported.")
        }
    }

    private fun validateReceiver(
        function: KFunction<*>,
        invalid: (String) -> Nothing
    ) {
        val parameters = function.parameters

        if (
            parameters.firstOrNull()?.kind !=
            KParameter.Kind.INSTANCE
        ) {
            invalid("Step must be an instance member function.")
        }

        if (
            parameters.any {
                it.kind == KParameter.Kind.EXTENSION_RECEIVER
            }
        ) {
            invalid("Extension receiver functions are not supported.")
        }

        val instanceCount = parameters.count {
            it.kind == KParameter.Kind.INSTANCE
        }

        if (instanceCount != 1) {
            invalid("Step must declare exactly one instance receiver.")
        }
    }

    private fun validateParameters(
        function: KFunction<*>,
        invalid: (String) -> Nothing
    ) {
        val parameters = function.parameters

        val valueParameters = parameters.filter {
            it.kind == KParameter.Kind.VALUE
        }

        if (valueParameters.size > 1) {
            invalid(
                "Step cannot declare more than one input parameter."
            )
        }

        if (parameters.any { it.isOptional }) {
            invalid("Default parameters are not supported.")
        }

        if (parameters.any { it.isVararg }) {
            invalid("Vararg parameters are not supported.")
        }

        if (
            parameters.any {
                it.kind != KParameter.Kind.INSTANCE &&
                        it.kind != KParameter.Kind.VALUE
            }
        ) {
            invalid("Unsupported function parameter kind.")
        }
    }

    private fun validateFunctionType(
        function: KFunction<*>,
        invalid: (String) -> Nothing
    ) {
        if (function.typeParameters.isNotEmpty()) {
            invalid("Generic step functions are not supported.")
        }

        val valueParameters = function.parameters.filter {
            it.kind == KParameter.Kind.VALUE
        }

        valueParameters.forEach { parameter ->
            if (parameter.type.classifier !is KClass<*>) {
                invalid(
                    "Step parameter '${parameter.name}' " +
                            "must have a resolvable runtime type."
                )
            }

            if (parameter.type.isMarkedNullable.not() &&
                parameter.type.classifier == Nothing::class
            ) {
                invalid(
                    "Nothing is not a supported step input type."
                )
            }
        }

        val instanceParameter = function.parameters.first()

        if (instanceParameter.type.classifier !is KClass<*>) {
            invalid("Step instance receiver type cannot be resolved.")
        }
    }

    private fun validateTimeout(
        timeoutMillis: Long,
        invalid: (String) -> Nothing
    ) {
        if (timeoutMillis < 0L) {
            invalid("timeoutMillis cannot be negative.")
        }

        if (
            timeoutMillis > 0L &&
            !timeoutMillis.milliseconds.isFinite()
        ) {
            invalid(
                "timeoutMillis exceeds the supported duration range."
            )
        }
    }

    private fun validateRetry(
        annotation: Step,
        invalid: (String) -> Nothing
    ) {
        val retry = annotation.retry

        if (retry.maxAttempts < 1) {
            invalid("retry.maxAttempts must be at least 1.")
        }

        if (retry.initialDelayMillis < 0L) {
            invalid(
                "retry.initialDelayMillis cannot be negative."
            )
        }

        if (retry.maxDelayMillis < retry.initialDelayMillis) {
            invalid(
                "retry.maxDelayMillis must be greater than or equal to " +
                        "retry.initialDelayMillis."
            )
        }

        if (
            !retry.multiplier.isFinite() ||
            retry.multiplier < 1.0
        ) {
            invalid(
                "retry.multiplier must be finite and " +
                        "greater than or equal to 1.0."
            )
        }

        val initialDelay =
            retry.initialDelayMillis.milliseconds

        val maxDelay =
            retry.maxDelayMillis.milliseconds

        if (
            !initialDelay.isFinite() ||
            !maxDelay.isFinite()
        ) {
            invalid(
                "Retry delay exceeds the supported duration range."
            )
        }
    }
}
