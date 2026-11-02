package io.github.kiwiwhale.kiwiflow.annotation.factory

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.metadata.WorkflowMetadata
import io.github.kiwiwhale.kiwiflow.annotation.metadata.WorkflowMetadataCache
import io.github.kiwiwhale.kiwiflow.annotation.reflection.StepInspector
import io.github.kiwiwhale.kiwiflow.annotation.reflection.StepInvoker
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import io.github.kiwiwhale.kiwiflow.core.KiwiFlow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow

object AnnotatedWorkflowFactory {

    private val workflowInspector = WorkflowInspector(StepInspector())

    private val metadataCache = WorkflowMetadataCache(workflowInspector)

    private val stepInvoker = StepInvoker()

    fun <I, O> fromInstance(
        instance: Any
    ): KiwiFlowWorkflow<I, O> {
        return fromInstance(
            instance = instance,
            targetClass = instance.javaClass
        )
    }

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

    fun <I, O> fromInstance(
        instance: Any,
        targetClass: Class<*>
    ): KiwiFlowWorkflow<I, O> {
        val metadata = metadataCache.get(targetClass)

        return create(instance, metadata)
    }

    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow,
        targetClass: Class<*>
    ): KiwiFlowWorkflow<I, O> {
        val metadata = metadataCache.get(
            targetClass = targetClass,
            annotation = annotation
        )

        return create(instance, metadata)
    }

    private fun <I, O> create(
        instance: Any,
        metadata: WorkflowMetadata
    ): KiwiFlowWorkflow<I, O> {
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
