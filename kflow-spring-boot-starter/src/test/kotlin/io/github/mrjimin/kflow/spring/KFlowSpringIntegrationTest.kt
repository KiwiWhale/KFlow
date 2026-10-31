package io.github.mrjimin.kflow.spring

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.spring.annotation.EnableKFlow
import io.github.mrjimin.kflow.spring.registry.WorkflowRegistry
import io.github.mrjimin.kflow.workflow.KFlowWorkflow
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KFlowSpringIntegrationTest {

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(TestApplication::class.java)

    @Test
    fun `EnableKFlow로 Workflow Bean이 자동 등록된다`() {
        contextRunner.run { context ->
            val registry = context.getBean<WorkflowRegistry>()

            assertTrue(registry.contains("TestWorkflow"))
        }
    }

    @Test
    fun `등록된 Workflow를 실행할 수 있다`() {
        contextRunner.run { context ->
            val registry = context.getBean<WorkflowRegistry>()

            @Suppress("UNCHECKED_CAST")
            val workflow = registry.get("TestWorkflow")
                    as KFlowWorkflow<String, String>

            val result = runBlocking {
                workflow.execute("KFlow")
            }

            assertEquals("Hello KFlow", result)
        }
    }

    @SpringBootConfiguration
    @EnableKFlow
    class TestApplication {

        @Bean
        fun testWorkflow() = TestWorkflow()
    }

    @Workflow(name = "TestWorkflow")
    class TestWorkflow {

        @Step(order = 1)
        fun execute(input: String): String {
            return "Hello $input"
        }
    }
}
