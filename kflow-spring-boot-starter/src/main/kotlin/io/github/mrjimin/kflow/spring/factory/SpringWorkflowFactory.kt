package io.github.mrjimin.kflow.spring.factory

import io.github.mrjimin.kflow.annotation.Workflow
import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.mrjimin.kflow.core.workflow.KFlowWorkflow
import org.springframework.aop.support.AopUtils

class SpringWorkflowFactory {

    fun create(
        instance: Any,
        annotation: Workflow
    ): KFlowWorkflow<Any?, Any?> {
        val targetClass = AopUtils.getTargetClass(instance)

        return AnnotatedWorkflowFactory.fromInstance(
            instance = instance,
            annotation = annotation,
            targetClass = targetClass
        )
    }
}
