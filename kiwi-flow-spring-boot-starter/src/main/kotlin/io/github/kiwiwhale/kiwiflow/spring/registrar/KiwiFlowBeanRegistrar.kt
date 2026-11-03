package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflowKey
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

    /**
     * Discovers and registers all eligible workflow beans.
     *
     * All discovered workflows are created and validated
     * before a single atomic registration operation.
     */
    override fun afterSingletonsInstantiated() {
        val discovered =
            linkedMapOf<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>()

        applicationContext.beanDefinitionNames.forEach { beanName ->
            discoverBean(beanName)?.forEach { (key, workflow) ->
                check(discovered.putIfAbsent(key, workflow) == null) {
                    "Workflow '${key.name}' is declared by multiple Spring beans."
                }
            }
        }

        registry.registerAll(discovered)
    }

    /**
     * Discovers workflows declared by a Spring bean.
     */
    private fun discoverBean(
        beanName: String
    ): Map<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>? {

        val beanType = applicationContext.getType(beanName)
            ?: return null

        val candidateAnnotations =
            findWorkflowAnnotations(beanType)

        if (candidateAnnotations.isEmpty()) {
            return null
        }

        val bean = applicationContext.getBean(beanName)

        val targetClass = AopUtils.getTargetClass(bean)

        val annotations = findWorkflowAnnotations(targetClass)
            .ifEmpty { candidateAnnotations }
            .sortedBy { it.name }

        val result =
            linkedMapOf<KiwiWorkflowKey<*, *>, KiwiWorkflow<*, *>>()

        annotations.forEach { annotation ->

            val name = annotation.name
                .takeIf(String::isNotBlank)
                ?: targetClass.simpleName
                    .takeIf(String::isNotBlank)
                ?: beanName

            val key = KiwiWorkflowKey<Any?, Any?>(name)

            check(key !in result) {
                "Workflow '$name' is declared multiple times " +
                        "in bean '$beanName'."
            }

            result[key] = factory.create(
                instance = bean,
                annotation = annotation
            )
        }

        return result
    }

    /**
     * Finds merged workflow annotations.
     *
     * Supports composed annotations and meta-annotations.
     */
    private fun findWorkflowAnnotations(
        targetClass: Class<*>
    ): Set<Workflow> =
        AnnotatedElementUtils.findAllMergedAnnotations(
            targetClass,
            Workflow::class.java
        )
}
