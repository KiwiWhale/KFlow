package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import kotlin.reflect.KClass

internal class WorkflowMetadataCache(
    private val inspector: WorkflowInspector
) {

    private val cache = object : ClassValue<WorkflowMetadata>() {

        override fun computeValue(type: Class<*>): WorkflowMetadata {
            return inspector.inspect(type.kotlin)
        }
    }

    fun get(targetClass: KClass<*>): WorkflowMetadata {
        return get(targetClass.java)
    }

    fun get(targetClass: Class<*>): WorkflowMetadata {
        return cache.get(targetClass)
    }

    fun get(targetClass: Class<*>, annotation: Workflow): WorkflowMetadata {
        return inspector.inspect(
            kClass = targetClass.kotlin,
            annotation = annotation
        )
    }
}
