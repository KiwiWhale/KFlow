package io.github.kiwiwhale.kiwiflow.spring.autoconfigure

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class KiwiFlowAutoConfigurationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(
            AutoConfigurations.of(KiwiFlowAutoConfiguration::class.java)
        )

    @Test
    fun `KFlow Bean들이 자동 구성된다`() {
        contextRunner.run { context ->
            assertNotNull(context.getBean<KiwiWorkflowRegistry>())
            assertNotNull(context.getBean<SpringWorkflowFactory>())
            assertNotNull(context.getBean<KiwiFlowBeanRegistrar>())
        }
    }

    @Test
    fun `사용자 정의 WorkflowRegistry를 유지한다`() {
        val customRegistry = KiwiWorkflowRegistry()

        contextRunner
            .withBean(
                "customWorkflowRegistry",
                KiwiWorkflowRegistry::class.java,
                { customRegistry }
            )
            .run { context ->
                assertNotNull(context.getBean<KiwiWorkflowRegistry>())

                assertSame(
                    customRegistry,
                    context.getBean<KiwiWorkflowRegistry>()
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
                assertSame(
                    customFactory,
                    context.getBean<SpringWorkflowFactory>()
                )
            }
    }
}
