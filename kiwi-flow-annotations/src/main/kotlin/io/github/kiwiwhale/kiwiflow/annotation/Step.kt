package io.github.kiwiwhale.kiwiflow.annotation

/**
 * Declares a method as an executable step within a KiwiFlow workflow.
 *
 * A step must be a public member function accepting zero or one
 * input parameter. The return value of each step is passed to
 * the next step as its input.
 *
 * Steps are executed in ascending order based on [order].
 * Steps with the same order are sorted alphabetically by their
 * resolved names.
 *
 * This annotation can also be used as a meta-annotation to define
 * reusable custom step annotations.
 *
 * Example:
 *
 * ```
 * @Step(
 *     name = "validate",
 *     order = 1,
 *     timeoutMillis = 5_000,
 *     retry = Retry(maxAttempts = 3)
 * )
 * fun validate(input: String): String = input
 * ```
 *
 * @property name The step name. When blank, the method name is used.
 * @property order The execution order of this step.
 * @property timeoutMillis The maximum execution duration in milliseconds.
 * A value of zero disables the timeout.
 * @property retry The retry configuration applied to this step.
 *
 * @see Workflow
 * @see Retry
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KiwiFlowAnnotation
annotation class Step(
    val name: String = "",
    val order: Int = 0,
    val timeoutMillis: Long = 0L,
    val retry: Retry = Retry()
)