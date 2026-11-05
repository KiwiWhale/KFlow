package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.KiwiFlowAnnotation
import kotlin.reflect.KClass

/**
 * Resolves KiwiFlow annotations from direct and composed annotations.
 *
 * Direct annotations are always considered.
 * Composed annotations are traversed only when their annotation
 * type is marked with [KiwiFlowAnnotation].
 *
 * Circular references and repeated annotation types within
 * a traversal path are safely ignored.
 */
internal object AnnotationResolver {

    /**
     * Finds an annotation of the requested type.
     *
     * @param annotations Annotations declared on the inspected element.
     * @param target Annotation type to resolve.
     * @return The resolved annotation, or null if none exists.
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
                "Multiple @${target.simpleName ?: target.qualifiedName} " +
                        "annotations were found."
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

    /**
     * Excludes JVM and Kotlin infrastructure annotations.
     */
    private fun isInfrastructureAnnotation(
        annotation: Annotation
    ): Boolean {
        val packageName =
            annotation.annotationClass.java.packageName

        return packageName == "java.lang.annotation" ||
                packageName.startsWith("kotlin.")
    }
}
