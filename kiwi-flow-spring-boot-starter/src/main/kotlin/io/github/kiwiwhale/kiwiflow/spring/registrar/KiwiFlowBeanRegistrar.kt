package io.github.kiwiwhale.kiwiflow.spring.registrar

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.spring.factory.SpringWorkflowFactory
import io.github.kiwiwhale.kiwiflow.spring.registry.WorkflowRegistry
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils

class KiwiFlowBeanRegistrar(
    private val applicationContext: ApplicationContext,
    private val factory: SpringWorkflowFactory,
    private val registry: WorkflowRegistry
) : SmartInitializingSingleton {

    override fun afterSingletonsInstantiated() {
        applicationContext.beanDefinitionNames.forEach { beanName ->
            registerBean(beanName)
        }
    }

    private fun registerBean(beanName: String) {
        val beanType = applicationContext.getType(beanName)
            ?: return

        val candidateAnnotations = findWorkflowAnnotations(beanType)

        if (candidateAnnotations.isEmpty()) {
            return
        }

        val bean = applicationContext.getBean(beanName)
        val targetClass = AopUtils.getTargetClass(bean)

        val annotations = findWorkflowAnnotations(targetClass)
            .ifEmpty { candidateAnnotations }

        annotations.forEach { annotation ->
            val name = annotation.name.ifBlank {
                targetClass.simpleName
            }

            registry.register(
                name = name,
                workflow = factory.create(
                    instance = bean,
                    annotation = annotation
                )
            )
        }
    }

    private fun findWorkflowAnnotations(
        targetClass: Class<*>
    ): Set<Workflow> {
        return AnnotatedElementUtils.findAllMergedAnnotations(
            targetClass,
            Workflow::class.java
        )
    }
}
