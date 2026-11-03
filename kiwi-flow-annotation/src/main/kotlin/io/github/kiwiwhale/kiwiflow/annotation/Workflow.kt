package io.github.kiwiwhale.kiwiflow.annotation

/**
 * Declares a class as a KiwiFlow workflow.
 *
 * A workflow consists of one or more methods annotated with [Step].
 * Annotated steps are discovered through reflection and executed
 * sequentially according to their configured order.
 *
 * This annotation can also be used as a meta-annotation to define
 * custom workflow annotations.
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
 * @property name The workflow name. When blank, the annotated class
 * name is used.
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