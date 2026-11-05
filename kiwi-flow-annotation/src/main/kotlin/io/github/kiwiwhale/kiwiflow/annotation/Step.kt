package io.github.kiwiwhale.kiwiflow.annotation

/**
 * Declares a function as an executable step in a KiwiFlow workflow.
 *
 * A step must be a public member function accepting zero or one
 * value parameter.
 *
 * Steps are executed sequentially. The result of each step is
 * passed to the next step as its input.
 *
 * A step returning [Unit] preserves the previous workflow data.
 * A nullable return value of null is treated as an actual result.
 *
 * Steps are ordered by ascending [order]. Steps with the same
 * order are sorted alphabetically by their resolved names.
 *
 * This annotation can be composed into custom annotations
 * using [KiwiFlowAnnotation].
 *
 * Example:
 *
 * ```
 * @Workflow(name = "user-registration")
 * class UserRegistrationWorkflow {
 *
 *     @Step(order = 1)
 *     fun validate(input: String): String = input
 *
 *     @Step(
 *         name = "register",
 *         order = 2,
 *         timeoutMillis = 5_000,
 *         retry = Retry(maxAttempts = 3)
 *     )
 *     suspend fun register(input: String): String = input
 * }
 * ```
 *
 * @property name Explicit step name. When blank, the function name
 * is used.
 * @property order Execution order of the step.
 * @property timeoutMillis Maximum execution duration in milliseconds.
 * Zero disables the timeout.
 * @property retry Retry configuration applied to the step.
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
