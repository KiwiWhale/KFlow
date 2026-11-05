package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Caches resolved workflow metadata.
 *
 * Default annotation discovery and explicit workflow
 * configurations are cached independently.
 */
internal class WorkflowMetadataCache(
    private val inspector: WorkflowInspector
) {

    private val defaultCache =
        ConcurrentHashMap<KClass<*>, WorkflowMetadata>()

    private val explicitCache =
        ConcurrentHashMap<ExplicitCacheKey, WorkflowMetadata>()

    fun get(targetClass: KClass<*>): WorkflowMetadata =
        defaultCache.computeIfAbsent(targetClass) {
            inspector.inspect(it)
        }

    fun get(
        targetClass: KClass<*>,
        annotation: Workflow
    ): WorkflowMetadata {
        val key = ExplicitCacheKey(
            targetClass = targetClass,
            annotation = annotation
        )

        return explicitCache.computeIfAbsent(key) {
            inspector.inspect(
                kClass = targetClass,
                annotation = annotation
            )
        }
    }

    fun evict(targetClass: KClass<*>) {
        defaultCache.remove(targetClass)

        explicitCache.keys.removeIf {
            it.targetClass == targetClass
        }
    }

    fun clear() {
        defaultCache.clear()
        explicitCache.clear()
    }

    val size: Int
        get() = defaultCache.size + explicitCache.size

    private data class ExplicitCacheKey(
        val targetClass: KClass<*>,
        val annotation: Workflow
    )
}
