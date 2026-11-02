package io.github.kiwiwhale.kiwiflow.annotation

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