package io.github.mrjimin.kflow.spring.factory

import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.mrjimin.kflow.workflow.KFlowWorkflow
import org.springframework.aop.framework.AopProxyUtils

class SpringWorkflowFactory {

    fun create(instance: Any): KFlowWorkflow<Any?, Any?> {
        val target = AopProxyUtils.getSingletonTarget(instance) ?: instance

        return AnnotatedWorkflowFactory.fromInstance(target)
    }
}
