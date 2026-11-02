package io.github.mrjimin.kflow.annotation

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@KFlowAnnotation
annotation class Step(
    val name: String = "",
    val order: Int = 0,
    val timeoutMillis: Long = 0L,
    val retry: Retry = Retry()
)