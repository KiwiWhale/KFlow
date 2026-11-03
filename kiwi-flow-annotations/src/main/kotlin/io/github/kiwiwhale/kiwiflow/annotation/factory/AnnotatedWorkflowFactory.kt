package io.github.kiwiwhale.kiwiflow.annotation.factory

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidWorkflowException
import io.github.kiwiwhale.kiwiflow.annotation.metadata.WorkflowMetadata
import io.github.kiwiwhale.kiwiflow.annotation.metadata.WorkflowMetadataCache
import io.github.kiwiwhale.kiwiflow.annotation.reflection.StepInspector
import io.github.kiwiwhale.kiwiflow.annotation.reflection.StepInvoker
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import kotlin.reflect.KClass

/**
 * Factory for creating [KiwiWorkflow] instances from annotated Kotlin objects.
 *
 * Discovers workflow and step metadata through Kotlin reflection
 * and converts annotated functions into executable workflow steps.
 *
 * Workflow metadata is cached by the target [KClass] to avoid
 * repeated reflection during workflow creation.
 *
 * The factory supports:
 *
 * - Workflow discovery through [Workflow].
 * - Step discovery through [io.github.kiwiwhale.kiwiflow.annotation.Step].
 * - Sequential step execution based on step ordering.
 * - Per-step timeout configuration.
 * - Per-step retry configuration.
 * - Regular and suspend Kotlin functions.
 *
 * Example:
 *
 * ```
 * @Workflow(name = "example")
 * class ExampleWorkflow {
 *
 *     @Step(order = 1)
 *     fun first(input: String): String = input
 *
 *     @Step(order = 2)
 *     suspend fun second(input: String): String = input
 * }
 *
 * val workflow = AnnotatedWorkflowFactory
 *     .fromInstance<String, String>(ExampleWorkflow())
 * ```
 *
 * @see WorkflowInspector
 * @see StepInspector
 * @see StepInvoker
 */
object AnnotatedWorkflowFactory {

    private val workflowInspector = WorkflowInspector(
        stepInspector = StepInspector()
    )

    private val metadataCache = WorkflowMetadataCache(
        inspector = workflowInspector
    )

    private val stepInvoker = StepInvoker()

    /**
     * Creates a workflow from an annotated instance.
     *
     * The Kotlin runtime class of [instance] is used for metadata discovery.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Annotated workflow instance.
     * @return Executable workflow.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun <I, O> fromInstance(
        instance: Any
    ): KiwiWorkflow<I, O> {
        return fromInstance(
            instance = instance,
            targetClass = instance::class
        )
    }

    /**
     * Creates a workflow using an explicitly supplied annotation.
     *
     * The supplied annotation overrides annotation discovery
     * on the target class.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Workflow instance.
     * @param annotation Workflow annotation.
     * @return Executable workflow.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow
    ): KiwiWorkflow<I, O> {
        return fromInstance(
            instance = instance,
            annotation = annotation,
            targetClass = instance::class
        )
    }

    /**
     * Creates a workflow using an explicitly supplied Kotlin class.
     *
     * This overload is useful when the instance is accessed through
     * a proxy, superclass, or another compatible runtime type.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Workflow instance.
     * @param targetClass Kotlin class used for metadata discovery.
     * @return Executable workflow.
     * @throws IllegalArgumentException If the instance is incompatible
     * with the target class.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun <I, O> fromInstance(
        instance: Any,
        targetClass: KClass<*>
    ): KiwiWorkflow<I, O> {
        validateInstance(instance, targetClass)

        val metadata = metadataCache.get(targetClass)

        return create(
            instance = instance,
            metadata = metadata
        )
    }

    /**
     * Creates a workflow using an explicit annotation and Kotlin class.
     *
     * This overload allows callers to override both the workflow
     * annotation and the class used for metadata discovery.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Workflow instance.
     * @param annotation Workflow annotation.
     * @param targetClass Kotlin class used for metadata discovery.
     * @return Executable workflow.
     * @throws IllegalArgumentException If the instance is incompatible
     * with the target class.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow,
        targetClass: KClass<*>
    ): KiwiWorkflow<I, O> {
        validateInstance(instance, targetClass)

        val metadata = metadataCache.get(
            targetClass = targetClass,
            annotation = annotation
        )

        return create(
            instance = instance,
            metadata = metadata
        )
    }

    /**
     * Validates that the supplied instance is compatible with
     * the class used for metadata discovery.
     */
    private fun validateInstance(
        instance: Any,
        targetClass: KClass<*>
    ) {
        require(targetClass.isInstance(instance)) {
            "Instance of ${instance::class.qualifiedName} is not compatible with ${targetClass.qualifiedName}."
        }
    }

    /**
     * Converts workflow metadata into an executable workflow.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Annotated workflow instance.
     * @param metadata Discovered workflow metadata.
     * @return Executable workflow.
     */
    private fun <I, O> create(
        instance: Any,
        metadata: WorkflowMetadata
    ): KiwiWorkflow<I, O> {

        val key = KiwiWorkflowKey<I, O>(
            name = metadata.name
        )

        return KiwiFlow.workflow(key) {

            var currentData: Any? = input

            for (stepMetadata in metadata.steps) {
                val stepInput = currentData

                currentData = step(stepMetadata.name) {

                    stepMetadata.timeout?.let(::timeout)
                    stepMetadata.retryPolicy?.let(::retry)

                    execute {
                        stepInvoker.invoke(
                            metadata = stepMetadata,
                            instance = instance,
                            input = stepInput
                        )
                    }
                }
            }

            @Suppress("UNCHECKED_CAST")
            currentData as O
        }
    }
}
