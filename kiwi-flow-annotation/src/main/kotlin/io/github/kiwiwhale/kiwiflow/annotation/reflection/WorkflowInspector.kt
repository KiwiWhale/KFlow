package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidWorkflowException
import io.github.kiwiwhale.kiwiflow.annotation.exception.KiwiFlowAnnotationException
import io.github.kiwiwhale.kiwiflow.annotation.metadata.WorkflowMetadata
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions

/**
 * Discovers and validates annotated KiwiFlow workflows.
 *
 * Inspects workflow annotations, discovers their step methods,
 * validates step metadata, and produces an ordered workflow
 * metadata representation.
 *
 * Inherited member functions are included in discovery because
 * inspection uses Kotlin's [kotlin.reflect.full.memberFunctions].
 */
internal class WorkflowInspector(
    private val stepInspector: StepInspector = StepInspector()
) {

    /**
     * Discovers workflow configuration from the specified class.
     *
     * The class must have a direct or composed [Workflow] annotation.
     *
     * @param kClass Class to inspect.
     * @return Resolved workflow metadata.
     * @throws InvalidWorkflowException If the workflow annotation
     * is missing, ambiguous, or invalid.
     */
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

    /**
     * Inspects a class using the supplied workflow configuration.
     *
     * Discovers all applicable step methods, validates their metadata,
     * checks for duplicate step names, and sorts them by execution order.
     *
     * @param kClass Class containing workflow step methods.
     * @param annotation Workflow configuration to apply.
     * @return Resolved workflow metadata.
     * @throws InvalidStepException If a discovered step is invalid.
     * @throws InvalidWorkflowException If workflow validation fails.
     */
    fun inspect(
        kClass: KClass<*>,
        annotation: Workflow
    ): WorkflowMetadata {
        val workflowName = annotation.name.ifBlank {
            kClass.simpleName ?: "AnonymousWorkflow"
        }

        val steps = try {
            kClass.memberFunctions.mapNotNull(stepInspector::inspect)
        } catch (exception: KiwiFlowAnnotationException) {
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
