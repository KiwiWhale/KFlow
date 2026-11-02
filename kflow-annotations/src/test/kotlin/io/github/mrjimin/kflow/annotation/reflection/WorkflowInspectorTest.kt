package io.github.mrjimin.kflow.annotation.reflection

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.exception.InvalidWorkflowException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Workflow(name = "composed-workflow")
annotation class CustomWorkflow

class WorkflowInspectorTest {

    private val inspector = WorkflowInspector()

    @Workflow
    class DefaultWorkflow {

        @Step
        fun first() {}

        @Step
        fun second() {}
    }

    @Workflow(name = "custom-workflow")
    class CustomNamedWorkflow {

        @Step(order = 20)
        fun second() {}

        @Step(order = 10)
        fun first() {}
    }

    @Workflow
    class OrderedWorkflow {

        @Step(order = 2)
        fun third() {}

        @Step(order = 1)
        fun second() {}

        @Step(order = 0)
        fun first() {}
    }

    @Workflow
    class DuplicateWorkflow {

        @Step(name = "duplicate")
        fun first() {}

        @Step(name = "duplicate")
        fun second() {}
    }

    @Workflow
    class EmptyWorkflow

    class UnannotatedWorkflow {

        @Step
        fun execute() {}
    }

    @CustomWorkflow
    class ComposedWorkflowClass {

        @Step
        fun execute() {}
    }

    @Test
    fun `기본 워크플로우 이름을 사용한다`() {
        val metadata = inspector.inspect(DefaultWorkflow::class)

        assertEquals("DefaultWorkflow", metadata.name)
    }

    @Test
    fun `사용자 지정 워크플로우 이름을 사용한다`() {
        val metadata = inspector.inspect(CustomNamedWorkflow::class)

        assertEquals("custom-workflow", metadata.name)
    }

    @Test
    fun `워크플로우의 모든 단계를 수집한다`() {
        val metadata = inspector.inspect(DefaultWorkflow::class)

        assertEquals(2, metadata.steps.size)
    }

    @Test
    fun `단계 순서에 따라 오름차순으로 정렬한다`() {
        val metadata = inspector.inspect(OrderedWorkflow::class)

        assertEquals(
            listOf("first", "second", "third"),
            metadata.steps.map { it.name }
        )
    }

    @Test
    fun `단계 순서가 같으면 이름을 기준으로 정렬한다`() {
        val metadata = inspector.inspect(DefaultWorkflow::class)

        assertEquals(
            listOf("first", "second"),
            metadata.steps.map { it.name }
        )
    }

    @Test
    fun `중복된 단계 이름이 있으면 예외가 발생한다`() {
        val exception = assertFailsWith<InvalidWorkflowException> {
            inspector.inspect(DuplicateWorkflow::class)
        }

        assertEquals(
            "Invalid KFlow workflow 'DuplicateWorkflow': Duplicate step names: duplicate.",
            exception.message
        )
    }

    @Test
    fun `단계가 없는 워크플로우는 예외가 발생한다`() {
        val exception = assertFailsWith<InvalidWorkflowException> {
            inspector.inspect(EmptyWorkflow::class)
        }

        assertEquals(
            "Invalid KFlow workflow 'EmptyWorkflow': At least one @Step method must be declared.",
            exception.message
        )
    }

    @Test
    fun `Workflow가 없는 클래스는 예외가 발생한다`() {
        val exception = assertFailsWith<InvalidWorkflowException> {
            inspector.inspect(UnannotatedWorkflow::class)
        }

        assertEquals(
            "Invalid KFlow workflow 'UnannotatedWorkflow': Class must be annotated with @Workflow.",
            exception.message
        )
    }

    @Test
    fun `커스텀 메타 어노테이션을 통해 Workflow를 생성한다`() {
        val metadata = inspector.inspect(ComposedWorkflowClass::class)

        assertEquals("composed-workflow", metadata.name)
        assertEquals(1, metadata.steps.size)
    }
}
