package io.github.mrjimin.kflow.spring.autoconfigure

import io.github.mrjimin.kflow.spring.factory.SpringWorkflowFactory
import io.github.mrjimin.kflow.spring.registrar.KFlowBeanRegistrar
import io.github.mrjimin.kflow.spring.registry.WorkflowRegistry
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.Test
import kotlin.test.assertNotNull

class KFlowAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(KFlowAutoConfiguration::class.java)
        )

    @Test
    fun `KFlow Bean들이 자동 구성된다`() {
        contextRunner.run { context ->
            assertNotNull(context.getBean<WorkflowRegistry>())
            assertNotNull(context.getBean<SpringWorkflowFactory>())
            assertNotNull(context.getBean<KFlowBeanRegistrar>())
        }
    }

    @Test
    fun `사용자 정의 WorkflowRegistry를 유지한다`() {
        val customRegistry = WorkflowRegistry()

        contextRunner
            .withBean(
                "customWorkflowRegistry",
                WorkflowRegistry::class.java,
                { customRegistry }
            )
            .run { context ->
                assertNotNull(context.getBean<WorkflowRegistry>())

                kotlin.test.assertSame(
                    customRegistry,
                    context.getBean<WorkflowRegistry>()
                )
            }
    }

    @Test
    fun `사용자 정의 SpringWorkflowFactory를 유지한다`() {
        val customFactory = SpringWorkflowFactory()

        contextRunner
            .withBean(
                "customSpringWorkflowFactory",
                SpringWorkflowFactory::class.java,
                { customFactory }
            )
            .run { context ->
                kotlin.test.assertSame(
                    customFactory,
                    context.getBean<SpringWorkflowFactory>()
                )
            }
    }
}