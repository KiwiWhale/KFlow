package io.github.mrjimin.kflow.spring

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.spring.annotation.EnableKFlow
import io.github.mrjimin.kflow.spring.registry.WorkflowRegistry
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
@EnableKFlow
class KFlowSpringIntegrationTest {

    @Autowired
    lateinit var registry: WorkflowRegistry

    @Test
    fun `Workflow 어노테이션이 붙은 Bean이 자동 등록된다`() {
        assertTrue(registry.contains("TestWorkflow"))
    }

    @Workflow(name = "TestWorkflow")
    class TestWorkflow {

        @Step(order = 1)
        fun execute(input: String): String {
            return "Hello $input"
        }
    }
}