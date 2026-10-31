package io.github.mrjimin.kflow.annotation.metadata

import io.github.mrjimin.kflow.annotation.reflection.WorkflowInspector
import kotlin.reflect.KClass

internal class WorkflowMetadataCache(
    private val inspector: WorkflowInspector
) {

    private val cache = object : ClassValue<WorkflowMetadata>() {

        override fun computeValue(type: Class<*>): WorkflowMetadata {
            return inspector.inspect(type.kotlin)
        }
    }

    fun get(kClass: Class<*>): WorkflowMetadata =
        cache.get(kClass)

    fun get(kClass: KClass<*>): WorkflowMetadata =
        cache.get(kClass.java)
}
