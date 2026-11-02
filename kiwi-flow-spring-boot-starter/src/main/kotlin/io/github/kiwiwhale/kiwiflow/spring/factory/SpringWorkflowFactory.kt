package io.github.kiwiwhale.kiwiflow.spring.factory

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import org.springframework.aop.support.AopUtils

class SpringWorkflowFactory {

    fun create(
        instance: Any,
        annotation: Workflow
    ): KiwiFlowWorkflow<Any?, Any?> {
        val targetClass = AopUtils.getTargetClass(instance)

        return AnnotatedWorkflowFactory.fromInstance(
            instance = instance,
            annotation = annotation,
            targetClass = targetClass
        )
    }
}
