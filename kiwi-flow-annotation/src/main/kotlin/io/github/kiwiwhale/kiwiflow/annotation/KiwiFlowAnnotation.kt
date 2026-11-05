package io.github.kiwiwhale.kiwiflow.annotation

/**
 * Marks a custom annotation as part of the KiwiFlow annotation model.
 *
 * This marker enables recursive meta-annotation resolution for
 * custom annotations that compose KiwiFlow annotations.
 *
 * Apply this annotation to custom annotations that directly or
 * indirectly reference [Workflow], [Step], or other KiwiFlow
 * configuration annotations.
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
 *
 * @see Workflow
 * @see Step
 * @see Retry
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class KiwiFlowAnnotation
