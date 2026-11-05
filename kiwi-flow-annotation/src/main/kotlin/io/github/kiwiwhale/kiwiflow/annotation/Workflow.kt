package io.github.kiwiwhale.kiwiflow.annotation

/**
 * Declares a class as a KiwiFlow workflow.
 *
 * A workflow consists of one or more functions annotated with [Step].
 * Steps are discovered through Kotlin reflection and executed
 * sequentially according to their configured order.
 *
 * Inherited member functions are also considered during discovery.
 *
 * This annotation can be composed into custom workflow annotations
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
 *     @Step(order = 2)
 *     fun register(input: String): String = input
 * }
 * ```
 *
 * @property name Explicit workflow name. When blank, the class
 * simple name is used.
 *
 * @see Step
 * @see KiwiFlowAnnotation
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KiwiFlowAnnotation
annotation class Workflow(
    val name: String = ""
)
