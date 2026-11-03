package io.github.kiwiwhale.kiwiflow.annotation

/**
 * Marks an annotation as a KiwiFlow-related annotation.
 *
 * This annotation allows custom annotations to participate in
 * KiwiFlow meta-annotation resolution.
 *
 * Apply this annotation to custom annotations that compose
 * [Workflow], [Step], or other KiwiFlow annotations.
 *
 * Example:
 *
 * ```
 * @KiwiFlowAnnotation
 * @Target(AnnotationTarget.FUNCTION)
 * @Retention(AnnotationRetention.RUNTIME)
 * @Step(name = "validate")
 * annotation class ValidateStep
 * ```
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class KiwiFlowAnnotation