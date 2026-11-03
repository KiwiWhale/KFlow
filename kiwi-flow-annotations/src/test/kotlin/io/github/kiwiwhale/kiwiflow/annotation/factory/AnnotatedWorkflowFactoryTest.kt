package io.github.kiwiwhale.kiwiflow.annotation.factory

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidWorkflowException
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class AnnotatedWorkflowFactoryTest {

    @Workflow
    class StringWorkflow {

        @Step(order = 1)
        fun trim(input: String): String {
            return input.trim()
        }

        @Step(order = 2)
        fun uppercase(input: String): String {
            return input.uppercase()
        }
    }

    @Workflow(name = "custom-workflow")
    class CustomWorkflow {

        @Step
        fun execute() {}
    }

    @Workflow
    class EmptyWorkflow

    class UnannotatedWorkflow

    private val instance = StringWorkflow()

    @Test
    fun `어노테이션이 적용된 인스턴스로 워크플로우를 생성한다`() {
        val kiwiWorkflow: KiwiWorkflow<String, String> =
            AnnotatedWorkflowFactory.fromInstance(instance)

        assertNotNull(kiwiWorkflow)
    }

    @Test
    fun `사용자 지정 워크플로우 이름으로 생성할 수 있다`() {
        val kiwiWorkflow: KiwiWorkflow<String, String> =
            AnnotatedWorkflowFactory.fromInstance(
                CustomWorkflow()
            )

        assertNotNull(kiwiWorkflow)
    }

    @Test
    fun `잘못된 인스턴스는 워크플로우 생성에 실패한다`() {
        assertFailsWith<InvalidWorkflowException> {
            AnnotatedWorkflowFactory.fromInstance<Any?, Any?>(
                UnannotatedWorkflow()
            )
        }
    }

    @Test
    fun `단계가 없는 인스턴스는 워크플로우 생성에 실패한다`() {
        assertFailsWith<InvalidWorkflowException> {
            AnnotatedWorkflowFactory.fromInstance<Any?, Any?>(
                EmptyWorkflow()
            )
        }
    }

    @Test
    fun `동일한 인스턴스로 여러 워크플로우를 생성할 수 있다`() {
        val first: KiwiWorkflow<String, String> =
            AnnotatedWorkflowFactory.fromInstance(instance)

        val second: KiwiWorkflow<String, String> =
            AnnotatedWorkflowFactory.fromInstance(instance)

        assertNotNull(first)
        assertNotNull(second)
    }

    @Test
    fun `워크플로우 생성 시 원본 인스턴스의 상태를 변경하지 않는다`() {
        val original = instance

        AnnotatedWorkflowFactory.fromInstance<String, String>(instance)

        assertSame(original, instance)
    }
}
