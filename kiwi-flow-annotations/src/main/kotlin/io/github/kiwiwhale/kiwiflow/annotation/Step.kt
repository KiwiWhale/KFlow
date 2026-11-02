package io.github.kiwiwhale.kiwiflow.annotation

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