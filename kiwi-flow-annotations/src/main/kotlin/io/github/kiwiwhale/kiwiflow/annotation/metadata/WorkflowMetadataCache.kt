package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import kotlin.reflect.KClass

/**
 * Caches workflow metadata by target class.
 *
 * Uses [ClassValue] to associate metadata with a class without
 * maintaining a global strong-reference map of class loaders.
 *
 * Workflows using an explicitly supplied [Workflow] annotation
 * are inspected independently and do not use the class-based cache.
 *
 * @property inspector Inspector responsible for metadata discovery.
 */
internal class WorkflowMetadataCache(
    private val inspector: WorkflowInspector
) {

    private val cache = object : ClassValue<WorkflowMetadata>() {
        override fun computeValue(type: Class<*>): WorkflowMetadata {
            return inspector.inspect(type.kotlin)
        }
    }

    /**
     * Returns cached metadata for the specified Kotlin class.
     *
     * @param targetClass Class to inspect.
     * @return Resolved workflow metadata.
     */
    fun get(targetClass: KClass<*>): WorkflowMetadata {
        return get(targetClass.java)
    }

    /**
     * Returns cached metadata for the specified Java class.
     *
     * @param targetClass Class to inspect.
     * @return Resolved workflow metadata.
     */
    fun get(targetClass: Class<*>): WorkflowMetadata {
        return cache.get(targetClass)
    }

    /**
     * Inspects a workflow using an explicitly supplied annotation.
     *
     * This operation bypasses the class-based metadata cache.
     *
     * @param targetClass Class containing workflow step methods.
     * @param annotation Workflow configuration to use.
     * @return Resolved workflow metadata.
     */
    fun get(targetClass: Class<*>, annotation: Workflow): WorkflowMetadata {
        return inspector.inspect(
            kClass = targetClass.kotlin,
            annotation = annotation
        )
    }
}
