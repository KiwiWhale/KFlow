package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registry.WorkflowRegistry
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.registerBean

class KiwiFlowDuplicateWorkflowTest {

    @Workflow(name = "DuplicateWorkflow")
    class FirstWorkflow {

        @Step
        fun execute(input: String) = "First $input"
    }

    @Workflow(name = "DuplicateWorkflow")
    class SecondWorkflow {

        @Step
        fun execute(input: String) = "Second $input"
    }

    @Test
    fun `서로 다른 Bean이 동일한 Workflow 이름을 사용하면 예외가 발생한다`() {
        val context = AnnotationConfigApplicationContext()

        val registry = WorkflowRegistry()
        val factory = SpringWorkflowFactory()

        context.registerBean("firstWorkflow") {
            FirstWorkflow()
        }

        context.registerBean("secondWorkflow") {
            SecondWorkflow()
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

        try {
            val exception = assertFailsWith<Throwable> {
                context.refresh()
            }

            val hasDuplicateException =
                generateSequence<Throwable>(exception) { it.cause }
                    .any { it is IllegalArgumentException }

            assertTrue(
                hasDuplicateException,
                "중복 Workflow 등록으로 IllegalArgumentException이 발생해야 합니다."
            )
        } finally {
            context.close()
        }
    }
}