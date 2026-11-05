package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import kotlin.reflect.full.memberFunctions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AnnotationResolverTest {

    @Target(AnnotationTarget.ANNOTATION_CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Step(name = "validate")
    annotation class Validate

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @Validate
    annotation class StrictValidate

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @Step(name = "ignored")
    annotation class Unmarked

    @Target(AnnotationTarget.ANNOTATION_CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Step(name = "first")
    annotation class FirstStep

    @Target(AnnotationTarget.ANNOTATION_CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Step(name = "second")
    annotation class SecondStep

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @FirstStep
    annotation class FirstComposition

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @SecondStep
    annotation class SecondComposition

    class TestWorkflow {

        @StrictValidate
        fun first() = Unit

        @Unmarked
        fun second() = Unit

        @Step(name = "direct")
        fun direct() = Unit

        @FirstComposition
        @SecondComposition
        fun duplicated() = Unit

        fun empty() = Unit
    }

    private fun annotations(
        functionName: String
    ): Array<out Annotation> {
        val function = TestWorkflow::class.memberFunctions
            .first { it.name == functionName }

        return function.annotations.toTypedArray()
    }

    @Test
    fun `중첩된 조합 어노테이션을 탐색한다`() {
        val result = AnnotationResolver.find(
            annotations = annotations("first"),
            target = Step::class
        )

        assertEquals("validate", result?.name)
    }

    @Test
    fun `마커가 없는 조합 어노테이션은 무시한다`() {
        val result = AnnotationResolver.find(
            annotations = annotations("second"),
            target = Step::class
        )

        assertEquals("ignored", result?.name)
    }

    @Test
    fun `직접 선언된 어노테이션을 탐색한다`() {
        val result = AnnotationResolver.find(
            annotations = annotations("direct"),
            target = Step::class
        )

        assertEquals("direct", result?.name)
    }

    @Test
    fun `대상 어노테이션이 없으면 null을 반환한다`() {
        val result = AnnotationResolver.find(
            annotations = annotations("empty"),
            target = Step::class
        )

        assertNull(result)
    }

    @Test
    fun `서로 다른 조합 어노테이션에서 중복된 Step을 발견하면 예외가 발생한다`() {
        assertFailsWith<IllegalArgumentException> {
            AnnotationResolver.find(
                annotations = annotations("duplicated"),
                target = Step::class
            )
        }
    }

    @Test
    fun `조합 어노테이션의 설정값을 유지한다`() {
        val result = AnnotationResolver.find(
            annotations = annotations("first"),
            target = Step::class
        )

        assertEquals("validate", result?.name)
    }
}
