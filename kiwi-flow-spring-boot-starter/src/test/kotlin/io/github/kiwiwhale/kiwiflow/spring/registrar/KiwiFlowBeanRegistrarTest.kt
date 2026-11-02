package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registry.WorkflowRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.registerBean

class KiwiFlowBeanRegistrarTest {

    @Workflow(name = "TestWorkflow")
    class TestWorkflow {

        @Step
        fun execute(input: String) = input
    }

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Workflow(name = "MetaWorkflow")
    annotation class MetaWorkflow

    @MetaWorkflow
    class MetaTestWorkflow {

        @Step
        fun execute(input: String) = "Meta $input"
    }

    @Test
    fun `Workflow 어노테이션이 있는 Bean을 자동 등록한다`() {
        val context = createContext {
            TestWorkflow()
        }

        context.use {
            val registry = it.getBean(WorkflowRegistry::class.java)

            assertTrue(registry.contains("TestWorkflow"))
            assertEquals(setOf("TestWorkflow"), registry.names())
        }
    }

    @Test
    fun `Meta-annotation으로 선언된 Workflow도 자동 등록한다`() {
        val context = createContext {
            MetaTestWorkflow()
        }

        context.use {
            val registry = it.getBean(WorkflowRegistry::class.java)

            assertTrue(registry.contains("MetaWorkflow"))
            assertEquals(setOf("MetaWorkflow"), registry.names())
        }
    }

    private fun createContext(
        workflowProvider: () -> Any
    ): AnnotationConfigApplicationContext {
        val context = AnnotationConfigApplicationContext()

        val registry = WorkflowRegistry()
        val factory = SpringWorkflowFactory()

        context.registerBean("testWorkflow") {
            workflowProvider()
        }

        context.registerBean {
            registry
        }

        context.registerBean {
            factory
        }

        context.registerBean {
            KiwiFlowBeanRegistrar(
                applicationContext = context,
                factory = factory,
                registry = registry
            )
        }

        context.refresh()

        return context
    }
}
