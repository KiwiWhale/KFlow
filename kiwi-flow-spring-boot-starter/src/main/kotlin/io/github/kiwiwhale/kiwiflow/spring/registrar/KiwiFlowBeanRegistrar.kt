package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowRegistry
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * Discovers Spring-managed beans annotated with [Workflow]
 * and registers them as executable workflows.
 *
 * Workflow registration is delegated to Core.
 */
class KiwiFlowBeanRegistrar(
    private val applicationContext: ApplicationContext,
    private val factory: SpringWorkflowFactory,
    private val registry: KiwiWorkflowRegistry
) : SmartInitializingSingleton {

    override fun afterSingletonsInstantiated() {
        val discovered = mutableListOf<KiwiWorkflow<*, *>>()

        applicationContext.beanDefinitionNames.forEach { beanName ->
            discoverBean(beanName)?.let(discovered::addAll)
        }

        registry.registerAll(discovered)
    }

    private fun discoverBean(
        beanName: String
    ): List<KiwiWorkflow<*, *>>? {
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

        val result = mutableListOf<KiwiWorkflow<*, *>>()
        val names = mutableSetOf<String>()

        annotations.forEach { annotation ->
            val name = annotation.name
                .takeIf(String::isNotBlank)
                ?: targetClass.simpleName
                    .takeIf(String::isNotBlank)
                ?: beanName

            check(names.add(name)) {
                "Workflow '$name' is declared multiple times " +
                        "in bean '$beanName'."
            }

            result += factory.create(
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
