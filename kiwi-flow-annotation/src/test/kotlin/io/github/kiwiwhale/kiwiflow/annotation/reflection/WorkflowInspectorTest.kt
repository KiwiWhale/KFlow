package io.github.kiwiwhale.kiwiflow.annotation.reflection

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.exception.InvalidWorkflowException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WorkflowInspectorTest {

    open class BaseWorkflow {

        @Step(order = 1)
        fun initialize(input: String): String = input
    }

    @Workflow(name = "user-workflow")
    class UserWorkflow : BaseWorkflow() {

        @Step(order = 2)
        fun execute(input: String): String = input
    }

    @Workflow(name = "duplicate-workflow")
    class DuplicateWorkflow {

        @Step(name = "process", order = 1)
        fun first(input: String) = input

        @Step(name = "process", order = 2)
        fun second(input: String) = input
    }

    private val inspector = WorkflowInspector()

    @Test
    fun `상속된 워크플로우 단계를 탐색한다`() {
        val metadata = inspector.inspect(UserWorkflow::class)

        assertEquals("user-workflow", metadata.name)
        assertEquals(2, metadata.steps.size)

        assertEquals(
            listOf("initialize", "execute"),
            metadata.steps.map { it.name }
        )
    }

    @Test
    fun `중복된 단계 이름을 거부한다`() {
        assertFailsWith<InvalidWorkflowException> {
            inspector.inspect(DuplicateWorkflow::class)
        }
    }
}
