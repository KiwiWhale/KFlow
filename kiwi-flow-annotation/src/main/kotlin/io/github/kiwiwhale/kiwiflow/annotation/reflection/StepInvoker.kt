package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.metadata.StepMetadata
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy

/**
 * Invokes workflow steps using Kotlin reflection.
 *
 * Supports regular and suspend member functions with
 * zero or one input parameter.
 *
 * Arguments are mapped explicitly using KParameter
 * to avoid relying on reflection parameter ordering.
 */
internal class StepInvoker {

    /**
     * Invokes a workflow step.
     *
     * @param metadata Metadata describing the step.
     * @param instance Instance containing the step function.
     * @param input Input value passed to the step, if applicable.
     * @return Result produced by the step.
     *
     * @throws IllegalArgumentException If the instance or input
     * is incompatible with the reflected function.
     */
    suspend fun invoke(
        metadata: StepMetadata,
        instance: Any,
        input: Any?
    ): Any? {
        val function = metadata.function

        val instanceParameter = function.parameters
            .firstOrNull {
                it.kind == KParameter.Kind.INSTANCE
            }
            ?: throw IllegalStateException(
                "Step function must have an instance receiver: ${function.name}"
            )

        validateInstance(
            functionName = function.name,
            parameter = instanceParameter,
            instance = instance
        )

        val arguments = buildArguments(
            metadata = metadata,
            instanceParameter = instanceParameter,
            instance = instance,
            input = input
        )

        return if (function.isSuspend) {
            function.callSuspendBy(arguments)
        } else {
            function.callBy(arguments)
        }
    }

    private fun buildArguments(
        metadata: StepMetadata,
        instanceParameter: KParameter,
        instance: Any,
        input: Any?
    ): Map<KParameter, Any?> {
        val arguments = mutableMapOf<KParameter, Any?>(
            instanceParameter to instance
        )

        val inputParameter = metadata.inputParameter

        if (inputParameter != null) {
            validateInput(
                functionName = metadata.function.name,
                parameter = inputParameter,
                input = input
            )

            arguments[inputParameter] = input
        }

        return arguments
    }

    private fun validateInstance(
        functionName: String,
        parameter: KParameter,
        instance: Any
    ) {
        val expectedClass = parameter.type.classifier as? KClass<*>
            ?: throw IllegalStateException(
                "Cannot resolve instance receiver type: $functionName"
            )

        require(expectedClass.isInstance(instance)) {
            "Invalid invocation target for '$functionName'. " +
                    "Expected ${expectedClass.qualifiedName}, " +
                    "but received ${instance::class.qualifiedName}."
        }
    }

    private fun validateInput(
        functionName: String,
        parameter: KParameter,
        input: Any?
    ) {
        val expectedClass = parameter.type.classifier as? KClass<*>
            ?: throw IllegalStateException(
                "Cannot resolve input parameter type: $functionName"
            )

        if (input == null) {
            require(parameter.type.isMarkedNullable) {
                "Step '$functionName' does not accept null input."
            }

            return
        }

        require(expectedClass.isInstance(input)) {
            "Invalid input for step '$functionName'. " +
                    "Expected ${parameter.type}, " +
                    "but received ${input::class.qualifiedName}."
        }
    }
}
