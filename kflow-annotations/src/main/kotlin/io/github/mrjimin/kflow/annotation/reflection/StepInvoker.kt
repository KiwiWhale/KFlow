package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.metadata.StepMetadata
import kotlin.reflect.full.callSuspend

internal class StepInvoker {

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
