package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
import io.github.kiwiwhale.kiwiflow.core.workflow.bootstrap.KiwiWorkflowBootstrap
import io.github.kiwiwhale.kiwiflow.core.workflow.bootstrap.KiwiWorkflowHolder
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiregistry.bootstrap.BootstrapHolder
import io.github.kiwiwhale.kiwiregistry.bootstrap.RegistryHolder
import io.github.kiwiwhale.kiwiregistry.exception.BootstrapAlreadyInjectedException
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * Discovers Spring-managed beans annotated with [Workflow]
 * and registers them as executable workflows.
 *
 * Workflow creation is delegated to [SpringWorkflowFactory],
 * while registration and bootstrap lifecycle management are
 * delegated to the Core registry infrastructure.
 *
 * Discovery and duplicate validation are completed before
 * registry contributions begin.
 *
 * @property applicationContext Spring application context.
 * @property factory Factory responsible for creating workflows.
 * @property bootstrap Core bootstrap lifecycle.
 * @property holder Registry contribution holder.
 */
class KiwiFlowBeanRegistrar(
    private val applicationContext: ApplicationContext,
    private val factory: SpringWorkflowFactory,
    private val bootstrap: BootstrapHolder = KiwiWorkflowBootstrap,
    private val holder: RegistryHolder = KiwiWorkflowHolder
) : SmartInitializingSingleton {

    /**
     * Discovers and registers workflows after singleton initialization.
     *
     * Executes the Core lifecycle:
     * inject -> contribute -> build.
     *
     * If the bootstrap has already been injected, the existing
     * lifecycle is reused without invoking another build callback.
     */
    override fun afterSingletonsInstantiated() {
        val workflows = discoverWorkflows()

        val build = try {
            bootstrap.inject()
        } catch (_: BootstrapAlreadyInjectedException) {
            null
        }

        holder.registryBootstrap(bootstrap) {
            registry(KiwiWorkflowKey.WORKFLOWS) {
                workflows.forEach { workflow ->
                    add(workflow.name, workflow)
                }
            }
        }

        build?.invoke()
    }

    /**
     * Discovers all workflows declared by Spring beans.
     *
     * @return Workflows in deterministic discovery order.
     * @throws IllegalStateException If duplicate workflow names are found.
     */
    private fun discoverWorkflows(): List<KiwiWorkflow<*, *>> {
        val workflows = LinkedHashMap<String, KiwiWorkflow<*, *>>()

        for (beanName in applicationContext.beanDefinitionNames) {
            discoverBean(beanName).forEach { workflow ->
                check(workflows.putIfAbsent(workflow.name, workflow) == null) {
                    "Duplicate workflow name '${workflow.name}' found across Spring beans."
                }
            }
        }

        return workflows.values.toList()
    }

    /**
     * Discovers workflows declared by a single Spring bean.
     *
     * Beans without workflow annotations are skipped without
     * instantiating or resolving the bean.
     *
     * @param beanName Spring bean name.
     * @return Workflows declared by the bean.
     */
    private fun discoverBean(
        beanName: String
    ): List<KiwiWorkflow<*, *>> {
        val beanType = applicationContext.getType(beanName)
            ?: return emptyList()

        val candidateAnnotations = findWorkflowAnnotations(beanType)

        if (candidateAnnotations.isEmpty()) {
            return emptyList()
        }

        val bean = applicationContext.getBean(beanName)
        val targetClass = AopUtils.getTargetClass(bean)

        val annotations = findWorkflowAnnotations(targetClass)
            .ifEmpty { candidateAnnotations }
            .sortedBy { it.name }

        val workflows = ArrayList<KiwiWorkflow<*, *>>(annotations.size)

        for (annotation in annotations) {
            workflows += factory.create(
                instance = bean,
                annotation = annotation,
                targetClass = targetClass
            )
        }

        return workflows
    }

    /**
     * Finds direct and composed workflow annotations.
     *
     * @param targetClass Class to inspect.
     * @return Discovered workflow annotations.
     */
    private fun findWorkflowAnnotations(
        targetClass: Class<*>
    ): Set<Workflow> =
        AnnotatedElementUtils.findAllMergedAnnotations(
            targetClass,
            Workflow::class.java
        )
}
