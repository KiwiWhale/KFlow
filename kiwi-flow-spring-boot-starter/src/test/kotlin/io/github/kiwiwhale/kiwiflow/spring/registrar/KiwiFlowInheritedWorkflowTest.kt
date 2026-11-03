package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.registerBean

class KiwiFlowInheritedWorkflowTest {

    @Workflow(name = "ParentWorkflow")
    open class ParentWorkflow {

        @Step
        open fun execute(input: String): String {
            return "Parent $input"
        }
    }

    class ChildWorkflow : ParentWorkflow()

    @Test
    fun `상위 클래스에 선언된 Workflow도 자동 등록한다`() {
        val context = createContext()

        context.use {
            val registry = it.getBean(KiwiWorkflowRegistry::class.java)

            assertTrue(registry.contains("ParentWorkflow"))

            assertEquals(
                setOf("ParentWorkflow"),
                registry.names()
            )
        }
    }

    @Test
    fun `상속된 Workflow를 실행할 수 있다`() = runBlocking {
        val context = createContext()

        context.use {
            val registry = it.getBean(KiwiWorkflowRegistry::class.java)

            @Suppress("UNCHECKED_CAST")
            val kiwiWorkflow = registry.get("ParentWorkflow")
                    as KiwiWorkflow<String, String>

            assertEquals(
                "Parent KFlow",
                kiwiWorkflow.execute("KFlow")
            )
        }
    }

    private fun createContext(): AnnotationConfigApplicationContext {
        val context = AnnotationConfigApplicationContext()

        val registry = KiwiWorkflowRegistry()
        val factory = SpringWorkflowFactory()

        context.registerBean("childWorkflow") {
            ChildWorkflow()
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