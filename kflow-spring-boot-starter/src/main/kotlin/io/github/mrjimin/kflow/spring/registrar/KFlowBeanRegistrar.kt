package io.github.mrjimin.kflow.spring.registrar

import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.spring.factory.SpringWorkflowFactory
import io.github.mrjimin.kflow.spring.registry.WorkflowRegistry
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotatedElementUtils

class KFlowBeanRegistrar(
    private val applicationContext: ApplicationContext,
    private val factory: SpringWorkflowFactory,
    private val registry: WorkflowRegistry
) : SmartInitializingSingleton {

    override fun afterSingletonsInstantiated() {
        val beans = applicationContext.getBeansWithAnnotation<Workflow>()

        beans.forEach { (_, bean) ->
            val targetClass = AopUtils.getTargetClass(bean)

            val annotation = AnnotatedElementUtils.findMergedAnnotation(
                targetClass,
                Workflow::class.java
            ) ?: return@forEach

            val name = annotation.name.ifBlank {
                targetClass.simpleName
            }

            registry.register(
                name = name,
                workflow = factory.create(bean)
            )
        }
    }
}
