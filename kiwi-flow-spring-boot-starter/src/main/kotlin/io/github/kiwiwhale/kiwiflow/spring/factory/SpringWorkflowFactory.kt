package io.github.kiwiwhale.kiwiflow.spring.factory

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import org.springframework.aop.support.AopUtils

/**
 * Creates executable [KiwiWorkflow] instances from Spring-managed beans.
 *
 * Resolves Spring AOP proxies to their underlying target classes and delegates
 * workflow inspection and construction to [AnnotatedWorkflowFactory].
 *
 * This class isolates Spring-specific proxy handling from the annotation module.
 *
 * @see AnnotatedWorkflowFactory
 */
class SpringWorkflowFactory {

    /**
     * Creates a workflow from a Spring-managed bean.
     *
     * @param instance Spring-managed workflow bean.
     * @param annotation Workflow configuration to apply.
     * @return Executable workflow.
     */
    fun create(
        instance: Any,
        annotation: Workflow
    ): KiwiWorkflow<Any?, Any?> =
        create(
            instance = instance,
            annotation = annotation,
            targetClass = AopUtils.getTargetClass(instance)
        )

    /**
     * Creates a workflow using an explicitly resolved target class.
     *
     * Allows callers to reuse an already resolved Spring AOP target class.
     *
     * @param instance Spring-managed workflow bean.
     * @param annotation Workflow configuration to apply.
     * @param targetClass Underlying workflow implementation class.
     * @return Executable workflow.
     */
    internal fun create(
        instance: Any,
        annotation: Workflow,
        targetClass: Class<*>
    ): KiwiWorkflow<Any?, Any?> =
        AnnotatedWorkflowFactory.fromInstance(
            instance = instance,
            annotation = annotation,
            targetClass = targetClass.kotlin
        )
}
