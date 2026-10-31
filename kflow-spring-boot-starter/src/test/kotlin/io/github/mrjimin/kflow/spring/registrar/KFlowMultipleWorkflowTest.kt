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

class KFlowMultipleWorkflowTest {

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Workflow(name = "FirstWorkflow")
    annotation class FirstWorkflow

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Workflow(name = "SecondWorkflow")
    annotation class SecondWorkflow

    @FirstWorkflow
    @SecondWorkflow
    class MultiWorkflow {

        @Step
        fun execute(input: String) = input
    }

    @Test
    fun `하나의 Bean에 선언된 여러 Workflow를 모두 등록한다`() {
        val context = AnnotationConfigApplicationContext()

        val registry = WorkflowRegistry()
        val factory = SpringWorkflowFactory()

        context.registerBean("multiWorkflow") {
            MultiWorkflow()
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
            assertTrue(registry.contains("FirstWorkflow"))
            assertTrue(registry.contains("SecondWorkflow"))

            assertEquals(
                setOf("FirstWorkflow", "SecondWorkflow"),
                registry.names()
            )
        }
    }
}
