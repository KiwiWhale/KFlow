package io.github.kiwiwhale.kiwiflow.spring.autoconfigure

import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import io.github.kiwiwhale.kiwiflow.spring.registry.WorkflowRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
class KiwiFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun workflowRegistry(): WorkflowRegistry =
        WorkflowRegistry()

    @Bean
    @ConditionalOnMissingBean
    fun springWorkflowFactory(): SpringWorkflowFactory =
        SpringWorkflowFactory()

    @Bean
    @ConditionalOnMissingBean
    fun kiwiFlowBeanRegistrar(
        applicationContext: ApplicationContext,
        factory: SpringWorkflowFactory,
        registry: WorkflowRegistry
    ): KiwiFlowBeanRegistrar =
        KiwiFlowBeanRegistrar(
            applicationContext = applicationContext,
            factory = factory,
            registry = registry
        )
}
