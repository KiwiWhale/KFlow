package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidWorkflowException
import io.github.kiwiwhale.kiwiflow.annotation.exception.KiwiFlowAnnotationException
import io.github.kiwiwhale.kiwiflow.annotation.metadata.StepMetadata
import io.github.kiwiwhale.kiwiflow.annotation.metadata.WorkflowMetadata
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * Discovers and validates annotated KiwiFlow workflows.
 *
 * Inherited member functions are included in discovery.
 *
 * Execution order:
 * 1. Ascending step order.
 * 2. Alphabetical step name.
 */
internal class WorkflowInspector(
    private val stepInspector: StepInspector = StepInspector()
) {

    /**
     * Inspects a class using its declared or composed Workflow annotation.
     */
    fun inspect(kClass: KClass<*>): WorkflowMetadata {
        val annotation = resolveWorkflowAnnotation(kClass)

        return inspect(
            kClass = kClass,
            annotation = annotation
        )
    }

    /**
     * Inspects a class using an explicit Workflow configuration.
     */
    fun inspect(
        kClass: KClass<*>,
        annotation: Workflow
    ): WorkflowMetadata {
        val workflowName = resolveWorkflowName(
            kClass = kClass,
            annotation = annotation
        )

        val steps = discoverSteps(
            kClass = kClass,
            workflowName = workflowName
        )

        validateSteps(
            workflowName = workflowName,
            steps = steps
        )

        return WorkflowMetadata(
            name = workflowName,
            steps = steps.sortedWith(
                compareBy(
                    StepMetadata::order,
                    StepMetadata::name
                )
            )
        )
    }

    private fun resolveWorkflowAnnotation(
        kClass: KClass<*>
    ): Workflow =
        try {
            AnnotationResolver.find(
                annotations = kClass.annotations.toTypedArray(),
                target = Workflow::class
            ) ?: throw InvalidWorkflowException(
                workflowName = workflowNameOf(kClass),
                message = "Class must be annotated with @Workflow."
            )
        } catch (exception: KiwiFlowAnnotationException) {
            throw exception
        } catch (exception: Exception) {
            throw InvalidWorkflowException(
                workflowName = workflowNameOf(kClass),
                message = "Failed to resolve workflow annotation.",
                cause = exception
            )
        }

    private fun resolveWorkflowName(
        kClass: KClass<*>,
        annotation: Workflow
    ): String =
        annotation.name.takeIf(String::isNotBlank)
            ?: kClass.simpleName
                ?.takeIf(String::isNotBlank)
            ?: throw InvalidWorkflowException(
                workflowName = "Anonymous",
                message = "Workflow name could not be resolved."
            )

    private fun workflowNameOf(
        kClass: KClass<*>
    ): String =
        kClass.simpleName ?: "Anonymous"

    private fun discoverSteps(
        kClass: KClass<*>,
        workflowName: String
    ): List<StepMetadata> =
        try {
            kClass.memberFunctions.mapNotNull(
                stepInspector::inspect
            )
        } catch (exception: KiwiFlowAnnotationException) {
            throw exception
        } catch (exception: Exception) {
            throw InvalidWorkflowException(
                workflowName = workflowName,
                message = "Failed to discover workflow steps.",
                cause = exception
            )
        }

    private fun validateSteps(
        workflowName: String,
        steps: List<StepMetadata>
    ) {
        if (steps.isEmpty()) {
            throw InvalidWorkflowException(
                workflowName,
                "At least one @Step method must be declared."
            )
        }

        val duplicates = steps
            .groupingBy(StepMetadata::name)
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
    }
}
