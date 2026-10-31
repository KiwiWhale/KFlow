package io.github.mrjimin.kflow.spring.registrar

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.spring.factory.SpringWorkflowFactory
import io.github.mrjimin.kflow.spring.registry.WorkflowRegistry
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.registerBean
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KFlowBeanRegistrarTest {

    @Workflow(name = "TestWorkflow")
    class TestWorkflow {

        @Step
        fun execute(input: String) = input
    }

    @Test
    fun `Workflow 어노테이션이 있는 Bean을 자동 등록한다`() {
        val context = AnnotationConfigApplicationContext()

        val registry = WorkflowRegistry()
        val factory = SpringWorkflowFactory()

        context.registerBean("testWorkflow") {
            TestWorkflow()
        }

        context.registerBean {
            registry
        }

        context.registerBean {
            factory
        }

        context.registerBean {
            KFlowBeanRegistrar(
                applicationContext = context,
                factory = factory,
                registry = registry
            )
        }

        context.refresh()

        context.use {
            assertTrue(registry.contains("TestWorkflow"))
            assertEquals(
                setOf("TestWorkflow"),
                registry.names()
            )
        }
    }
}
