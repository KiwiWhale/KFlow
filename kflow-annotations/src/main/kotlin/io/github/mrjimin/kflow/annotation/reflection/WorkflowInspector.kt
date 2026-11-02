package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.exception.InvalidWorkflowException
import io.github.mrjimin.kflow.annotation.exception.KFlowAnnotationException
import io.github.mrjimin.kflow.annotation.metadata.WorkflowMetadata
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

internal class WorkflowInspector(
    private val stepInspector: StepInspector = StepInspector()
) {

    fun inspect(kClass: KClass<*>): WorkflowMetadata {
        val annotation = try {
            AnnotationResolver.find(
                annotations = kClass.annotations.toTypedArray(),
                target = Workflow::class
            )
        } catch (exception: IllegalArgumentException) {
            throw InvalidWorkflowException(
                kClass.simpleName ?: "Anonymous",
                "Multiple @Workflow annotations were found.",
                exception
            )
        } ?: throw InvalidWorkflowException(
            kClass.simpleName ?: "Anonymous",
            "Class must be annotated with @Workflow."
        )

        return inspect(kClass, annotation)
    }

    fun inspect(
        kClass: KClass<*>,
        annotation: Workflow
    ): WorkflowMetadata {
        val workflowName = annotation.name.ifBlank {
            kClass.simpleName ?: "AnonymousWorkflow"
        }

        val steps = try {
            kClass.memberFunctions.mapNotNull(stepInspector::inspect)
        } catch (exception: KFlowAnnotationException) {
            throw exception
        } catch (exception: Exception) {
            throw InvalidWorkflowException(
                workflowName,
                "Failed to inspect workflow steps.",
                exception
            )
        }

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
