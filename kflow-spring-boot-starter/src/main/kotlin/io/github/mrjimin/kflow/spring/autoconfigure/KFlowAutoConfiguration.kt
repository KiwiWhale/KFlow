package io.github.mrjimin.kflow.spring.autoconfigure

import io.github.mrjimin.kflow.spring.factory.SpringWorkflowFactory
import io.github.mrjimin.kflow.spring.registrar.KFlowBeanRegistrar
import io.github.mrjimin.kflow.spring.registry.WorkflowRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@AutoConfiguration
class KFlowAutoConfiguration {

    @Bean
    fun workflowRegistry() = WorkflowRegistry()

    @Bean
    fun springWorkflowFactory() = SpringWorkflowFactory()

    @Bean
    fun kFlowBeanRegistrar(
        applicationContext: ApplicationContext,
        factory: SpringWorkflowFactory,
        registry: WorkflowRegistry
    ) = KFlowBeanRegistrar(
        applicationContext,
        factory,
        registry
    )
}