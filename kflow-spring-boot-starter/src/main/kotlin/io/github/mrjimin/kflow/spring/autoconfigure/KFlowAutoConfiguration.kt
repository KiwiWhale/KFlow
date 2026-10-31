package io.github.mrjimin.kflow.spring.autoconfigure

import io.github.mrjimin.kflow.spring.factory.SpringWorkflowFactory
import io.github.mrjimin.kflow.spring.registrar.KFlowBeanRegistrar
import io.github.mrjimin.kflow.spring.registry.WorkflowRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
class KFlowAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun workflowRegistry() = WorkflowRegistry()

    @Bean
    @ConditionalOnMissingBean
    fun springWorkflowFactory() = SpringWorkflowFactory()

    @Bean
    @ConditionalOnMissingBean
    fun kFlowBeanRegistrar(
        applicationContext: ApplicationContext,
        factory: SpringWorkflowFactory,
        registry: WorkflowRegistry
    ) = KFlowBeanRegistrar(
        applicationContext = applicationContext,
        factory = factory,
        registry = registry
    )
}
