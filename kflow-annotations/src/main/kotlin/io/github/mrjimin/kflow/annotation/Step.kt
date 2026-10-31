package io.github.mrjimin.kflow.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Step(
    val name: String = "",
    val order: Int = 0,
    val timeoutMillis: Long = 0L,
    val retry: StepRetry = StepRetry()
)