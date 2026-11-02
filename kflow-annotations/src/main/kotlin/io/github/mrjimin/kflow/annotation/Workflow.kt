package io.github.mrjimin.kflow.annotation

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KFlowAnnotation
annotation class Workflow(
    val name: String = ""
)