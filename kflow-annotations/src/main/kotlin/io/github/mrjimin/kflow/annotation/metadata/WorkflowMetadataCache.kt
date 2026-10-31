package io.github.mrjimin.kflow.annotation.metadata

import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.reflection.WorkflowInspector
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

internal class WorkflowMetadataCache(
    private val inspector: WorkflowInspector
) {

    private val cache =
        object : ClassValue<ConcurrentHashMap<String, WorkflowMetadata>>() {

            override fun computeValue(type: Class<*>): ConcurrentHashMap<String, WorkflowMetadata> {
                return ConcurrentHashMap()
            }
        }

    fun get(kClass: Class<*>): WorkflowMetadata =
        cache.get(kClass).computeIfAbsent("") {
            inspector.inspect(kClass.kotlin)
        }

    fun get(
        kClass: Class<*>,
        annotation: Workflow
    ): WorkflowMetadata {
        return cache.get(kClass).computeIfAbsent(annotation.name) {
            inspector.inspect(kClass.kotlin, annotation)
        }
    }

    fun get(kClass: KClass<*>): WorkflowMetadata =
        get(kClass.java)
}
