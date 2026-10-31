package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.WorkflowSpec
import io.github.mrjimin.kflow.annotation.exception.InvalidWorkflowException
import io.github.mrjimin.kflow.annotation.metadata.WorkflowMetadata
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions

internal class WorkflowInspector(
    private val stepInspector: StepInspector = StepInspector()
) {

    fun inspect(kClass: KClass<*>): WorkflowMetadata {
        val annotation = kClass.findAnnotation<WorkflowSpec>()
            ?: throw InvalidWorkflowException(
                kClass.simpleName ?: "Anonymous",
                "Class must be annotated with @WorkflowSpec."
            )

        val workflowName = annotation.name.ifBlank {
            kClass.simpleName ?: "AnonymousWorkflow"
        }

        val steps = kClass.memberFunctions
            .mapNotNull(stepInspector::inspect)

        if (steps.isEmpty()) {
            throw InvalidWorkflowException(
                workflowName,
                "At least one @Step method must be declared."
            )
        }

        val duplicates = steps
            .groupingBy { it.name }
            .eachCount()
            .filterValues { it > 1 }
            .keys
            .sorted()

        if (duplicates.isNotEmpty()) {
            throw InvalidWorkflowException(
                workflowName,
                "Duplicate step names: ${duplicates.joinToString()}."
            )
        }

        return WorkflowMetadata(
            name = workflowName,
            steps = steps.sortedWith(
                compareBy(
                    { it.order },
                    { it.name }
                )
            )
        )
    }
}
