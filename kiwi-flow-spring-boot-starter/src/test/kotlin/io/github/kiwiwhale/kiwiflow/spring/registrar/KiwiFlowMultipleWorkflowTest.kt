package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.registerBean

class KiwiFlowMultipleWorkflowTest {

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

        val registry = KiwiWorkflowRegistry()
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
            KiwiFlowBeanRegistrar(
                applicationContext = context,
                factory = factory,
            )
        }

        context.refresh()

        context.use {
            assertTrue(registry.contains("FirstWorkflow"))
            assertTrue(registry.contains("SecondWorkflow"))

            assertEquals(
                setOf("FirstWorkflow", "SecondWorkflow"),
                registry.keys
            )
        }
    }
}
