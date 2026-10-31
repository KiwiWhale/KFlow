package io.github.mrjimin.kflow.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class WorkflowSpec(
    val name: String = ""
)