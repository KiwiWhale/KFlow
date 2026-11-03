package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.KiwiFlowAnnotation
import kotlin.reflect.KClass

/**
 * Resolves annotations declared directly or indirectly through
 * meta-annotation composition.
 *
 * This resolver is used to support custom annotations built on
 * top of KiwiFlow annotations.
 */
internal object AnnotationResolver {

    /**
     * Finds an annotation of the specified type.
     *
     * Searches direct annotations and their eligible meta-annotations.
     *
     * @param annotations Annotations to inspect.
     * @param target Annotation type to resolve.
     * @return The resolved annotation, or null if none is found.
     * @throws IllegalArgumentException If multiple matching annotations
     * are discovered.
     */
    fun <T : Annotation> find(
        annotations: Array<out Annotation>,
        target: KClass<T>
    ): T? {
        val candidates = mutableListOf<T>()

        annotations.forEach { annotation ->
            findMetaAnnotations(
                annotation = annotation,
                target = target.java,
                visited = mutableSetOf(),
                candidates = candidates
            )
        }

        return when (candidates.size) {
            0 -> null
            1 -> candidates.single()
            else -> throw IllegalArgumentException(
                "Multiple @${target.simpleName} annotations were found."
            )
        }
    }

    private fun <T : Annotation> findMetaAnnotations(
        annotation: Annotation,
        target: Class<T>,
        visited: MutableSet<Class<out Annotation>>,
        candidates: MutableList<T>
    ) {
        val annotationType = annotation.annotationClass.java

        if (target.isInstance(annotation)) {
            candidates += target.cast(annotation)
            return
        }

        if (!visited.add(annotationType)) {
            return
        }

        if (annotationType.getAnnotation(KiwiFlowAnnotation::class.java) == null) {
            return
        }

        annotationType.declaredAnnotations
            .asSequence()
            .filterNot(::isInfrastructureAnnotation)
            .forEach { metaAnnotation ->
                findMetaAnnotations(
                    annotation = metaAnnotation,
                    target = target,
                    visited = visited,
                    candidates = candidates
                )
            }
    }

    private fun isInfrastructureAnnotation(annotation: Annotation): Boolean {
        val packageName = annotation.annotationClass.java.packageName

        return packageName == "java.lang.annotation" ||
                packageName.startsWith("kotlin.")
    }
}