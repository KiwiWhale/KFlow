package io.github.mrjimin.kflow.spring.factory

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SpringWorkflowFactoryTest {

    @Workflow(name = "TestWorkflow")
    class TestWorkflow {

        @Step(order = 1)
        fun execute(input: String): String {
            return input.uppercase()
        }
    }

    private val factory = SpringWorkflowFactory()

    @Test
    fun `Spring Bean으로 Workflow를 생성한다`() = runBlocking {
        val workflow = factory.create(TestWorkflow())

        assertEquals(
            "HELLO",
            workflow.execute("hello")
        )
    }
}
