package io.github.kiwiwhale.kiwiflow.spring

import io.github.kiwiwhale.kiwiflow.annotation.Step
import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import io.github.kiwiwhale.kiwiflow.spring.registry.WorkflowRegistry
import kotlinx.coroutines.runBlocking
import org.aopalliance.intercept.MethodInterceptor
import org.springframework.aop.framework.ProxyFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.registerBean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KiwiFlowAopIntegrationTest {

    @Workflow(name = "AopWorkflow")
    open class AopWorkflow {

        @Step
        open fun execute(input: String): String {
            return "Hello $input"
        }
    }

    @Test
    fun `Spring AOP Proxy를 통해 Step을 실행한다`() = runBlocking {
        val invocationCount = AtomicInteger(0)

        val proxyFactory = ProxyFactory(AopWorkflow()).apply {
            isProxyTargetClass = true

            addAdvice(
                MethodInterceptor { invocation ->
                    invocationCount.incrementAndGet()
                    invocation.proceed()
                }
            )
        }

        val proxy = proxyFactory.proxy as AopWorkflow

        val context = AnnotationConfigApplicationContext()

        val registry = WorkflowRegistry()
        val factory = SpringWorkflowFactory()

        context.registerBean("aopWorkflow") {
            proxy
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

        context.use {
            assertTrue(registry.contains("AopWorkflow"))

            @Suppress("UNCHECKED_CAST")
            val workflow = registry.get("AopWorkflow")
                    as KiwiFlowWorkflow<String, String>

            assertEquals(
                "Hello KFlow",
                workflow.execute("KFlow")
            )

            assertEquals(1, invocationCount.get())
        }
    }
}
