package io.github.kiwiwhale.kiwiflow.annotation.factory

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
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
 * Factory for creating executable workflows from annotated Kotlin objects.
 *
 * The factory discovers workflow metadata through Kotlin reflection
 * and compiles the resolved metadata into a [KiwiWorkflow].
 *
 * Workflow creation consists of three stages:
 *
 * 1. Validate the workflow instance and target class.
 * 2. Resolve workflow metadata.
 * 3. Compile metadata into an executable workflow.
 *
 * Metadata discovery is cached for the default annotation-discovery path.
 * Explicit annotation configurations are inspected independently.
 *
 * This factory supports:
 *
 * - Direct and composed workflow annotations.
 * - Ordered step execution.
 * - Per-step timeout configuration.
 * - Per-step retry configuration.
 * - Regular and suspend functions.
 * - Zero-argument and single-input step functions.
 *
 * @see WorkflowInspector
 * @see StepInvoker
 * @see WorkflowMetadata
 */
object AnnotatedWorkflowFactory {

    private val metadataCache: WorkflowMetadataCache by lazy {
        WorkflowMetadataCache(
            inspector = WorkflowInspector(
                stepInspector = StepInspector()
            )
        )
    }

    private val stepInvoker: StepInvoker by lazy {
        StepInvoker()
    }

    /**
     * Creates a workflow using the runtime class of the instance.
     */
    fun <I, O> fromInstance(
        instance: Any
    ): KiwiWorkflow<I, O> =
        createWorkflow(
            instance = instance,
            targetClass = instance::class
        )

    /**
     * Creates a workflow using an explicitly supplied annotation.
     */
    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow
    ): KiwiWorkflow<I, O> =
        createWorkflow(
            instance = instance,
            targetClass = instance::class,
            annotation = annotation
        )

    /**
     * Creates a workflow using an explicitly supplied target class.
     *
     * Useful when the instance is accessed through a proxy,
     * superclass, or compatible runtime type.
     */
    fun <I, O> fromInstance(
        instance: Any,
        targetClass: KClass<*>
    ): KiwiWorkflow<I, O> =
        createWorkflow(
            instance = instance,
            targetClass = targetClass
        )

    /**
     * Creates a workflow using an explicit annotation and target class.
     */
    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow,
        targetClass: KClass<*>
    ): KiwiWorkflow<I, O> =
        createWorkflow(
            instance = instance,
            targetClass = targetClass,
            annotation = annotation
        )

    /**
     * Central workflow creation pipeline.
     *
     * All public factory methods delegate to this method.
     */
    private fun <I, O> createWorkflow(
        instance: Any,
        targetClass: KClass<*>,
        annotation: Workflow? = null
    ): KiwiWorkflow<I, O> {

        validateInstance(
            instance = instance,
            targetClass = targetClass
        )

        val metadata = resolveMetadata(
            targetClass = targetClass,
            annotation = annotation
        )

        return compile(
            instance = instance,
            metadata = metadata
        )
    }

    /**
     * Validates compatibility between the workflow instance
     * and the class used for metadata discovery.
     */
    private fun validateInstance(
        instance: Any,
        targetClass: KClass<*>
    ) {
        require(targetClass.isInstance(instance)) {
            "Workflow instance '${instance::class.qualifiedName}' " +
                    "is not compatible with target class " +
                    "'${targetClass.qualifiedName}'."
        }
    }

    /**
     * Resolves workflow metadata.
     *
     * Default annotation discovery uses the metadata cache.
     * Explicit annotation configurations bypass the cache.
     */
    private fun resolveMetadata(
        targetClass: KClass<*>,
        annotation: Workflow?
    ): WorkflowMetadata =
        if (annotation == null) {
            metadataCache.get(targetClass)
        } else {
            metadataCache.get(
                targetClass = targetClass,
                annotation = annotation
            )
        }

    /**
     * Compiles resolved metadata into an executable workflow.
     *
     * The resulting workflow delegates execution lifecycle,
     * tracking, timeout enforcement, and retry handling
     * to the core workflow implementation.
     */
    private fun <I, O> compile(
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

                    stepMetadata.timeout?.let {
                        timeout(it)
                    }

                    stepMetadata.retryPolicy?.let {
                        retry(it)
                    }

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
