package io.github.mrjimin.kflow.annotation.metadata

import io.github.mrjimin.kflow.annotation.reflection.WorkflowInspector

internal class WorkflowMetadataCache(
    private val inspector: WorkflowInspector
) {

    private val cache = object : ClassValue<WorkflowMetadata>() {

        override fun computeValue(
            type: Class<*>
        ): WorkflowMetadata {
            return inspector.inspect(type.kotlin)
        }
    }

    fun get(type: Class<*>): WorkflowMetadata =
        cache.get(type)
}
