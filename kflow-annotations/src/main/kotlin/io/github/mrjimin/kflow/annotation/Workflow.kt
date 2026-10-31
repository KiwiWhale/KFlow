package io.github.mrjimin.kflow.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Workflow(
    val name: String = ""
)