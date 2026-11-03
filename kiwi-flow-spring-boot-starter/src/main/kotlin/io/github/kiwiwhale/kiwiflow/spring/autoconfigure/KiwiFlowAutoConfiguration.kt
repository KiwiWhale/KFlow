package io.github.kiwiwhale.kiwiflow.spring.autoconfigure

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

/**
 * Auto-configuration for KiwiFlow's Spring integration.
 *
 * Core owns workflow registration and management.
 * Spring provides bean discovery and workflow creation.
 */
@AutoConfiguration
class KiwiFlowAutoConfiguration {

    /**
     * Creates the default Core workflow registry.
     */
    @Bean
    @ConditionalOnMissingBean(KiwiWorkflowRegistry::class)
    fun kiwiWorkflowRegistry(): KiwiWorkflowRegistry =
        KiwiWorkflowRegistry()

    /**
     * Creates the Spring workflow factory.
     */
    @Bean
    @ConditionalOnMissingBean
    fun springWorkflowFactory(): SpringWorkflowFactory =
        SpringWorkflowFactory()

    /**
     * Creates the workflow bean registrar.
     */
    @Bean
    @ConditionalOnMissingBean
    fun kiwiFlowBeanRegistrar(
        applicationContext: ApplicationContext,
        factory: SpringWorkflowFactory,
        registry: KiwiWorkflowRegistry
    ): KiwiFlowBeanRegistrar =
        KiwiFlowBeanRegistrar(
            applicationContext = applicationContext,
            factory = factory,
            registry = registry
        )
}
