package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registry.WorkflowRegistry
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * Discovers Spring-managed beans annotated with KiwiFlow's [Workflow]
 * annotation and registers them as executable workflows.
 *
 * Workflow discovery is performed after singleton initialization
 * through [SmartInitializingSingleton].
 *
 * All discovered workflows are created before being registered.
 * Registration is performed as a single batch to avoid partial
 * registry updates when workflow names conflict.
 *
 * @property applicationContext Spring application context.
 * @property factory Factory responsible for creating workflows.
 * @property registry Registry that stores discovered workflows.
 *
 * @see SpringWorkflowFactory
 * @see WorkflowRegistry
 */
class KiwiFlowBeanRegistrar(
    private val applicationContext: ApplicationContext,
    private val factory: SpringWorkflowFactory,
    private val registry: WorkflowRegistry
) : SmartInitializingSingleton {

    /**
     * Discovers and registers all eligible workflow beans.
     *
     * Invoked by Spring after all regular singleton beans have
     * been instantiated.
     *
     * @throws IllegalStateException If multiple workflows use
     * the same registration name.
     */
    override fun afterSingletonsInstantiated() {
        val discovered = linkedMapOf<String, KiwiFlowWorkflow<*, *>>()

        applicationContext.beanDefinitionNames.forEach { beanName ->
            discoverBean(beanName)?.forEach { (name, workflow) ->
                check(discovered.putIfAbsent(name, workflow) == null) {
                    "Workflow '$name' is declared by multiple Spring beans."
                }
            }
        }

        registry.registerAll(discovered)
    }

    private fun discoverBean(
        beanName: String
    ): Map<String, KiwiFlowWorkflow<*, *>>? {
        val beanType = applicationContext.getType(beanName)
            ?: return null

        val candidateAnnotations = findWorkflowAnnotations(beanType)

        if (candidateAnnotations.isEmpty()) {
            return null
        }

        val bean = applicationContext.getBean(beanName)
        val targetClass = AopUtils.getTargetClass(bean)

        val annotations = findWorkflowAnnotations(targetClass)
            .ifEmpty { candidateAnnotations }
            .sortedBy { it.name }


        val result = linkedMapOf<String, KiwiFlowWorkflow<*, *>>()

        annotations.forEach { annotation ->
            val name = annotation.name
                .takeIf(String::isNotBlank)
                ?: targetClass.simpleName.takeIf(String::isNotBlank)
                ?: beanName

            check(name !in result) {
                "Workflow '$name' is declared multiple times in bean '$beanName'."
            }

            result[name] = factory.create(
                instance = bean,
                annotation = annotation
            )
        }

        return result
    }

    private fun findWorkflowAnnotations(
        targetClass: Class<*>
    ): Set<Workflow> =
        AnnotatedElementUtils.findAllMergedAnnotations(
            targetClass,
            Workflow::class.java
        )
}
