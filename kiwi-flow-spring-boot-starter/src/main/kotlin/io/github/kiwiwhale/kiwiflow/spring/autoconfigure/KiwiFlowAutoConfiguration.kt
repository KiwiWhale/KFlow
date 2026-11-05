package io.github.kiwiwhale.kiwiflow.spring.autoconfigure

import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.core.workflow.bootstrap.KiwiWorkflowBootstrap
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import io.github.kiwiwhale.kiwiregistry.bootstrap.BootstrapHolder
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

/**
 * Autoconfiguration for KiwiFlow's Spring integration.
 *
 * Core owns workflow registration and management.
 * Spring provides bean discovery and workflow creation.
 */
@AutoConfiguration
class KiwiFlowAutoConfiguration {

    /**
     * Creates the BootstrapHolder bean if not provided.
     * Defaults to the Core's [KiwiWorkflowBootstrap].
     */
    @Bean
    @ConditionalOnMissingBean(BootstrapHolder::class)
    fun kiwiWorkflowBootstrap(): BootstrapHolder =
        KiwiWorkflowBootstrap

    /**
     * Creates the default Core workflow registry backed by the [BootstrapHolder].
     */
    @Bean
    @ConditionalOnMissingBean(KiwiWorkflowRegistry::class)
    fun kiwiWorkflowRegistry(bootstrap: BootstrapHolder): KiwiWorkflowRegistry =
        KiwiWorkflowRegistry(bootstrap)

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
        bootstrap: BootstrapHolder
    ): KiwiFlowBeanRegistrar =
        KiwiFlowBeanRegistrar(
            applicationContext = applicationContext,
            factory = factory,
            bootstrap = bootstrap
        )
}
