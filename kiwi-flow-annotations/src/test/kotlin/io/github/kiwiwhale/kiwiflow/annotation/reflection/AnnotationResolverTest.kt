package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import kotlin.test.*

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@Step(name = "composed-step", order = 10)
annotation class ComposedStep

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@ComposedStep
annotation class NestedStep

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@NestedStep
annotation class DeepNestedStep

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@Workflow(name = "composed-workflow")
annotation class ComposedWorkflow

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS
)
@Retention(AnnotationRetention.RUNTIME)
@ComposedWorkflow
annotation class NestedWorkflow

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Step(name = "first")
annotation class FirstStep

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Step(name = "second")
annotation class SecondStep

class AnnotationResolverTest {

    class SampleSteps {

        @ComposedStep
        fun composed() {}

        @NestedStep
        fun nested() {}

        @DeepNestedStep
        fun deepNested() {}

        fun unannotated() {}

        @FirstStep
        @SecondStep
        fun ambiguous() {}
    }

    @ComposedWorkflow
    class SampleWorkflow

    @NestedWorkflow
    class NestedSampleWorkflow

    @Test
    fun `직접 선언된 메타 어노테이션을 탐색한다`() {
        val function = SampleSteps::class.members
            .first { it.name == "composed" }

        val annotation = AnnotationResolver.find(
            function.annotations.toTypedArray(),
            Step::class
        )

        assertNotNull(annotation)
        assertEquals("composed-step", annotation.name)
        assertEquals(10, annotation.order)
    }

    @Test
    fun `한 단계 중첩된 메타 어노테이션을 탐색한다`() {
        val function = SampleSteps::class.members
            .first { it.name == "nested" }

        val annotation = AnnotationResolver.find(
            function.annotations.toTypedArray(),
            Step::class
        )

        assertNotNull(annotation)
        assertEquals("composed-step", annotation.name)
        assertEquals(10, annotation.order)
    }

    @Test
    fun `여러 단계 중첩된 메타 어노테이션을 탐색한다`() {
        val function = SampleSteps::class.members
            .first { it.name == "deepNested" }

        val annotation = AnnotationResolver.find(
            function.annotations.toTypedArray(),
            Step::class
        )

        assertNotNull(annotation)
        assertEquals("composed-step", annotation.name)
        assertEquals(10, annotation.order)
    }

    @Test
    fun `메타 어노테이션이 없으면 null을 반환한다`() {
        val function = SampleSteps::class.members
            .first { it.name == "unannotated" }

        val annotation = AnnotationResolver.find(
            function.annotations.toTypedArray(),
            Step::class
        )

        assertNull(annotation)
    }

    @Test
    fun `서로 다른 Step 메타 어노테이션이 있으면 예외가 발생한다`() {
        val function = SampleSteps::class.members
            .first { it.name == "ambiguous" }

        val exception = assertFailsWith<IllegalArgumentException> {
            AnnotationResolver.find(
                function.annotations.toTypedArray(),
                Step::class
            )
        }

        assertEquals(
            "Multiple @Step annotations were found.",
            exception.message
        )
    }

    @Test
    fun `Workflow 메타 어노테이션을 탐색한다`() {
        val annotation = AnnotationResolver.find(
            SampleWorkflow::class.annotations.toTypedArray(),
            Workflow::class
        )

        assertNotNull(annotation)
        assertEquals("composed-workflow", annotation.name)
    }

    @Test
    fun `중첩된 Workflow 메타 어노테이션을 탐색한다`() {
        val annotation = AnnotationResolver.find(
            NestedSampleWorkflow::class.annotations.toTypedArray(),
            Workflow::class
        )

        assertNotNull(annotation)
        assertEquals("composed-workflow", annotation.name)
    }
}
