package io.github.kiwiwhale.kiwiflow.spring.autoconfigure

import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registrar.KiwiFlowBeanRegistrar
import io.github.kiwiwhale.kiwiflow.spring.registry.WorkflowRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

/**
 * Auto-configuration for KiwiFlow's Spring integration.
 *
 * Registers the infrastructure beans required to discover,
 * create, and manage KiwiFlow workflows.
 *
 * Configuration can be customized by providing user-defined
 * beans of the corresponding types.
 *
 * @see WorkflowRegistry
 * @see SpringWorkflowFactory
 * @see KiwiFlowBeanRegistrar
 */
@AutoConfiguration
class KiwiFlowAutoConfiguration {

    /**
     * Creates the default workflow registry.
     *
     * @return Workflow registry bean.
     */
    @Bean
    @ConditionalOnMissingBean
    fun workflowRegistry(): WorkflowRegistry =
        WorkflowRegistry()

    /**
     * Creates the default Spring workflow factory.
     *
     * @return Spring workflow factory bean.
     */
    @Bean
    @ConditionalOnMissingBean
    fun springWorkflowFactory(): SpringWorkflowFactory =
        SpringWorkflowFactory()

    /**
     * Creates the workflow bean registrar.
     *
     * The registrar discovers annotated Spring beans after
     * singleton initialization and registers their workflows.
     *
     * @param applicationContext Spring application context.
     * @param factory Workflow factory.
     * @param registry Workflow registry.
     * @return Workflow bean registrar.
     */
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
