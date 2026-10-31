package io.github.mrjimin.kflow.spring.factory

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import kotlinx.coroutines.runBlocking
import org.springframework.core.annotation.AnnotatedElementUtils
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
        val instance = TestWorkflow()

        val annotation = requireNotNull(
            AnnotatedElementUtils.findMergedAnnotation(
                TestWorkflow::class.java,
                Workflow::class.java
            )
        )

        val workflow = factory.create(
            instance = instance,
            annotation = annotation
        )

        assertEquals(
            "HELLO",
            workflow.execute("hello")
        )
    }
}
