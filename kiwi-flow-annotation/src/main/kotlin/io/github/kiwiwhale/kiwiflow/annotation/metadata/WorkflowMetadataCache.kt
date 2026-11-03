package io.github.kiwiwhale.kiwiflow.annotation.metadata

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.reflection.WorkflowInspector
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Caches resolved workflow metadata by Kotlin class.
 *
 * Metadata discovery is performed only once per class for the
 * default annotation-discovery path.
 *
 * Explicit workflow annotations are inspected independently because
 * they may represent different workflow configurations for the
 * same target class.
 *
 * @property inspector Inspector responsible for resolving workflow metadata.
 *
 * @see WorkflowInspector
 * @see WorkflowMetadata
 */
internal class WorkflowMetadataCache(
    private val inspector: WorkflowInspector
) {

    private val cache =
        ConcurrentHashMap<KClass<*>, WorkflowMetadata>()

    /**
     * Returns cached metadata for the specified workflow class.
     *
     * If metadata has not been resolved yet, it is inspected
     * and stored in the cache.
     *
     * @param targetClass Kotlin class to inspect.
     * @return Resolved workflow metadata.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun get(targetClass: KClass<*>): WorkflowMetadata =
        cache.computeIfAbsent(targetClass, inspector::inspect)

    /**
     * Resolves workflow metadata using an explicit annotation.
     *
     * Explicit annotations bypass the default metadata cache
     * because their configuration may differ from the annotation
     * declared on the target class.
     *
     * @param targetClass Kotlin class to inspect.
     * @param annotation Explicit workflow annotation.
     * @return Resolved workflow metadata.
     * @throws InvalidWorkflowException If workflow metadata is invalid.
     */
    fun get(targetClass: KClass<*>, annotation: Workflow): WorkflowMetadata =
        inspector.inspect(targetClass, annotation)
}
