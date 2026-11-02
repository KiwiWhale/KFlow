package io.github.mrjimin.kflow.annotation.factory

import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.metadata.WorkflowMetadata
import io.github.mrjimin.kflow.annotation.metadata.WorkflowMetadataCache
import io.github.mrjimin.kflow.annotation.reflection.StepInspector
import io.github.mrjimin.kflow.annotation.reflection.StepInvoker
import io.github.mrjimin.kflow.annotation.reflection.WorkflowInspector
import io.github.mrjimin.kflow.core.KFlow
import io.github.mrjimin.kflow.core.workflow.KFlowWorkflow

object AnnotatedWorkflowFactory {

    private val workflowInspector = WorkflowInspector(StepInspector())

    private val metadataCache = WorkflowMetadataCache(workflowInspector)

    private val stepInvoker = StepInvoker()

    fun <I, O> fromInstance(
        instance: Any
    ): KFlowWorkflow<I, O> {
        return fromInstance(
            instance = instance,
            targetClass = instance.javaClass
        )
    }

    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow
    ): KFlowWorkflow<I, O> {
        return fromInstance(
            instance = instance,
            annotation = annotation,
            targetClass = instance.javaClass
        )
    }

    fun <I, O> fromInstance(
        instance: Any,
        targetClass: Class<*>
    ): KFlowWorkflow<I, O> {
        val metadata = metadataCache.get(targetClass)

        return create(instance, metadata)
    }

    fun <I, O> fromInstance(
        instance: Any,
        annotation: Workflow,
        targetClass: Class<*>
    ): KFlowWorkflow<I, O> {
        val metadata = metadataCache.get(
            targetClass = targetClass,
            annotation = annotation
        )

        return create(instance, metadata)
    }

    private fun <I, O> create(
        instance: Any,
        metadata: WorkflowMetadata
    ): KFlowWorkflow<I, O> {
        return KFlow.workflow(metadata.name) {

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
