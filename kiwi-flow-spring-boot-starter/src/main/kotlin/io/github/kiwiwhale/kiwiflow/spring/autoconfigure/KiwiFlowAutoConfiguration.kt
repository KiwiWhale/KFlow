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
 * Provides Spring Boot autoconfiguration for KiwiFlow.
 *
 * Configures the default Core bootstrap, workflow registry,
 * Spring workflow factory, and workflow bean registrar.
 *
 * Each infrastructure component can be replaced by providing
 * a custom Spring bean of the corresponding type.
 *
 * Core remains responsible for workflow registration and lifecycle
 * management, while Spring provides bean discovery and dependency injection.
 *
 * @see KiwiWorkflowBootstrap
 * @see KiwiWorkflowRegistry
 * @see SpringWorkflowFactory
 * @see KiwiFlowBeanRegistrar
 */
@AutoConfiguration
class KiwiFlowAutoConfiguration {

    /**
     * Provides the default Core bootstrap.
     *
     * Uses [KiwiWorkflowBootstrap] unless the application provides
     * another [BootstrapHolder] implementation.
     *
     * @return Core workflow bootstrap.
     */
    @Bean
    @ConditionalOnMissingBean(BootstrapHolder::class)
    fun kiwiWorkflowBootstrap(): BootstrapHolder =
        KiwiWorkflowBootstrap

    /**
     * Provides the default Core workflow registry.
     *
     * The registry delegates workflow access to the configured bootstrap.
     * Applications can replace it with a custom [KiwiWorkflowRegistry].
     *
     * @param bootstrap Bootstrap used by the registry.
     * @return Core workflow registry.
     */
    @Bean
    @ConditionalOnMissingBean(KiwiWorkflowRegistry::class)
    fun kiwiWorkflowRegistry(
        bootstrap: BootstrapHolder
    ): KiwiWorkflowRegistry =
        KiwiWorkflowRegistry(bootstrap)

    /**
     * Provides the default Spring workflow factory.
     *
     * Applications can replace this factory to customize
     * the creation of executable workflows.
     *
     * @return Spring workflow factory.
     */
    @Bean
    @ConditionalOnMissingBean(SpringWorkflowFactory::class)
    fun springWorkflowFactory(): SpringWorkflowFactory =
        SpringWorkflowFactory()

    /**
     * Provides the default workflow bean registrar.
     *
     * The registrar discovers annotated Spring beans and contributes
     * the resulting workflows to the Core registry.
     *
     * @param applicationContext Spring application context.
     * @param factory Factory used to construct workflows.
     * @param bootstrap Core bootstrap used for registration.
     * @return Workflow bean registrar.
     */
    @Bean
    @ConditionalOnMissingBean(KiwiFlowBeanRegistrar::class)
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
