package io.github.mrjimin.kflow.spring

import io.github.mrjimin.kflow.annotation.Step
import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.core.workflow.KFlowWorkflow
import io.github.mrjimin.kflow.spring.factory.SpringWorkflowFactory
import io.github.mrjimin.kflow.spring.registrar.KFlowBeanRegistrar
import io.github.mrjimin.kflow.spring.registry.WorkflowRegistry
import kotlinx.coroutines.runBlocking
import org.aopalliance.intercept.MethodInterceptor
import org.springframework.aop.framework.ProxyFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.registerBean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KFlowAopIntegrationTest {

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
            KFlowBeanRegistrar(
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
                    as KFlowWorkflow<String, String>

            assertEquals(
                "Hello KFlow",
                workflow.execute("KFlow")
            )

            assertEquals(1, invocationCount.get())
        }
    }
}
