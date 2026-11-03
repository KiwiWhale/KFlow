package io.github.kiwiwhale.kiwiflow.annotation.factory

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.metadata.WorkflowMetadata
import io.github.kiwiwhale.kiwiflow.annotation.metadata.WorkflowMetadataCache
import io.github.kiwiwhale.kiwiflow.annotation.reflection.StepInspector
import io.github.kiwiwhale.kiwiflow.annotation.reflection.StepInvoker
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow

/**
 * Factory for creating KiwiFlow workflows from annotated instances.
 *
 * This factory discovers workflow and step metadata through reflection
 * and converts annotated methods into executable KiwiFlow steps.
 *
 * Workflow metadata is cached by target class to avoid repeated
 * reflection during workflow creation.
 *
 * The factory supports:
 * - Workflow discovery through [Workflow].
 * - Step discovery through [Step].
 * - Sequential execution based on step ordering.
 * - Per-step timeout configuration.
 * - Per-step retry configuration.
 * - Suspend and regular Kotlin functions.
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
 *     .fromInstance< String, String >(ExampleWorkflow())
 * ```
 *
 * @see WorkflowInspector
 * @see StepInspector
 * @see StepInvoker
 */
object AnnotatedWorkflowFactory {

    private val workflowInspector = WorkflowInspector(StepInspector())

    private val metadataCache = WorkflowMetadataCache(workflowInspector)

    private val stepInvoker = StepInvoker()

    /**
     * Creates a workflow from an annotated instance.
     *
     * The runtime class of [instance] is used for workflow discovery.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Annotated workflow instance.
     * @return An executable KiwiFlow workflow.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun <I, O> fromInstance(
        instance: Any
    ): KiwiFlowWorkflow<I, O>{
        return fromInstance(
            instance = instance,
            targetClass = instance.javaClass
        )
    }

    /**
     * Creates a workflow using an explicitly supplied workflow annotation.
     *
     * The supplied annotation overrides annotation discovery on
     * the target class.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Workflow instance.
     * @param annotation Workflow configuration to use.
     * @return An executable KiwiFlow workflow.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow
    ): KiwiFlowWorkflow<I, O> {
        return fromInstance(
            instance = instance,
            annotation = annotation,
            targetClass = instance.javaClass
        )
    }

    /**
     * Creates a workflow using an explicitly specified target class.
     *
     * The target class determines which methods are inspected.
     * This is useful when the supplied instance is accessed through
     * a superclass or interface.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Workflow instance.
     * @param targetClass Class used for workflow metadata discovery.
     * @return An executable KiwiFlow workflow.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun <I, O> fromInstance(
        instance: Any,
        targetClass: Class<*>
    ): KiwiFlowWorkflow<I, O> {
        val metadata = metadataCache.get(targetClass)

        return create(instance, targetClass, metadata)
    }

    /**
     * Creates a workflow using an explicit annotation and target class.
     *
     * This overload allows callers to override both the annotation
     * configuration and the class used for metadata discovery.
     *
     * @param I Workflow input type.
     * @param O Workflow output type.
     * @param instance Workflow instance.
     * @param annotation Workflow configuration to use.
     * @param targetClass Class used for workflow metadata discovery.
     * @return An executable KiwiFlow workflow.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow,
        targetClass: Class<*>
    ): KiwiFlowWorkflow<I, O> {
        val metadata = metadataCache.get(
            targetClass = targetClass,
            annotation = annotation
        )

        return create(instance, targetClass, metadata)
    }

    private fun <I, O> create(
        instance: Any,
        targetClass: Class<*>,
        metadata: WorkflowMetadata
    ): KiwiFlowWorkflow<I, O> {

        require(targetClass.isInstance(instance)) {
            "Instance of ${instance.javaClass.name} is not compatible with ${targetClass.name}."
        }

        return KiwiFlow.workflow(metadata.name) {
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
