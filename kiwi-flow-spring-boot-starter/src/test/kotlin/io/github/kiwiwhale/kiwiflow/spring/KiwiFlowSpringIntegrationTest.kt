package io.github.kiwiwhale.kiwiflow.spring

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import io.github.kiwiwhale.kiwiflow.spring.annotation.EnableKiwiFlow
import io.github.kiwiwhale.kiwiflow.spring.registry.WorkflowRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean

class KiwiFlowSpringIntegrationTest {

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
                    as KiwiFlowWorkflow<String, String>

            val result = runBlocking {
                workflow.execute("KFlow")
            }

            assertEquals("Hello KFlow", result)
        }
    }

    @Test
    fun `Meta-annotation으로 선언된 Workflow도 자동 등록된다`() {
        contextRunner
            .withUserConfiguration(MetaWorkflowConfiguration::class.java)
            .run { context ->
                val registry = context.getBean<WorkflowRegistry>()

                assertTrue(registry.contains("MetaWorkflow"))
            }
    }

    @Test
    fun `Workflow 이름이 비어 있으면 클래스 이름으로 등록된다`() {
        contextRunner
            .withUserConfiguration(DefaultNameConfiguration::class.java)
            .run { context ->
                val registry = context.getBean<WorkflowRegistry>()

                assertTrue(registry.contains("DefaultNameWorkflow"))
            }
    }

    @SpringBootConfiguration
    @EnableKiwiFlow
    class TestApplication {

        @Bean
        fun testWorkflow() = TestWorkflow()
    }

    @SpringBootConfiguration
    @EnableKiwiFlow
    class MetaWorkflowConfiguration {

        @Bean
        fun metaTestWorkflow() = MetaTestWorkflow()
    }

    @SpringBootConfiguration
    @EnableKiwiFlow
    class DefaultNameConfiguration {

        @Bean
        fun defaultNameWorkflow() = DefaultNameWorkflow()
    }

    @Workflow(name = "TestWorkflow")
    class TestWorkflow {

        @Step(order = 1)
        fun execute(input: String): String {
            return "Hello $input"
        }
    }

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Workflow(name = "MetaWorkflow")
    annotation class MetaWorkflow

    @MetaWorkflow
    class MetaTestWorkflow {

        @Step(order = 1)
        fun execute(input: String): String {
            return "Meta $input"
        }
    }

    @Workflow
    class DefaultNameWorkflow {

        @Step(order = 1)
        fun execute(input: String): String {
            return input
        }
    }
}
