package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.metadata.StepMetadata
import kotlin.reflect.full.callSuspend

/**
 * Invokes workflow step functions through Kotlin reflection.
 *
 * Supports both regular and suspend member functions, with
 * optional input parameter forwarding.
 */
internal class StepInvoker {

    /**
     * Invokes the specified workflow step.
     *
     * The supplied input is passed to the step when it declares
     * an input parameter. Parameterless steps are invoked without
     * an input argument.
     *
     * Suspend functions are invoked through [KFunction.callSuspend].
     *
     * @param metadata Metadata describing the step.
     * @param instance Instance containing the step function.
     * @param input Input value passed to the step, if applicable.
     * @return Result produced by the step.
     * @throws Exception If reflective invocation fails.
     */
    suspend fun invoke(
        metadata: StepMetadata,
        instance: Any,
        input: Any?
    ): Any? {
        val function = metadata.function
        val parameter = metadata.inputParameter

        return when {
            function.isSuspend && parameter != null ->
                function.callSuspend(instance, input)

            function.isSuspend ->
                function.callSuspend(instance)

            parameter != null ->
                function.call(instance, input)

            else ->
                function.call(instance)
        }
    }
}
