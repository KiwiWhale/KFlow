package io.github.mrjimin.kflow.spring.factory

import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.mrjimin.kflow.core.workflow.KFlowWorkflow
import org.springframework.aop.framework.AopProxyUtils

class SpringWorkflowFactory {

    fun create(
        instance: Any,
        annotation: Workflow
    ): KFlowWorkflow<Any?, Any?> {
        val target = AopProxyUtils.getSingletonTarget(instance) ?: instance

        return AnnotatedWorkflowFactory.fromInstance(
            instance = target,
            annotation = annotation
        )
    }
}
