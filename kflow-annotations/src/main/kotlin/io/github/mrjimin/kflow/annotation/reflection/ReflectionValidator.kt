package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.exception.InvalidStepException
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility

internal object ReflectionValidator {

    fun validateStep(
        function: KFunction<*>,
        annotation: Step
    ) {
        val name = annotation.name.ifBlank {
            function.name
        }

        fun invalid(message: String): Nothing {
            throw InvalidStepException(name, message)
        }

        if (function.visibility != KVisibility.PUBLIC) {
            invalid("Step method must be public.")
        }

        val parameters = function.parameters

        val validParameters =
            parameters.size in 1..2 &&
                    parameters.firstOrNull()?.kind == KParameter.Kind.INSTANCE &&
                    parameters.drop(1).all {
                        it.kind == KParameter.Kind.VALUE
                    }

        if (!validParameters) {
            invalid(
                "Step must be a member function with zero or one input parameter."
            )
        }

        if (parameters.any { it.isOptional || it.isVararg }) {
            invalid(
                "Default and vararg parameters are not supported."
            )
        }

        if (annotation.timeoutMillis < 0L) {
            invalid("timeoutMillis cannot be negative.")
        }

        val retry = annotation.retry

        if (retry.maxAttempts < 1) {
            invalid("maxAttempts must be at least 1.")
        }

        if (retry.initialDelayMillis < 0L) {
            invalid("initialDelayMillis cannot be negative.")
        }

        if (retry.maxDelayMillis < retry.initialDelayMillis) {
            invalid(
                "maxDelayMillis must be greater than or equal to initialDelayMillis."
            )
        }

        if (!retry.multiplier.isFinite() || retry.multiplier < 1.0) {
            invalid(
                "multiplier must be finite and greater than or equal to 1.0."
            )
        }
    }
}
