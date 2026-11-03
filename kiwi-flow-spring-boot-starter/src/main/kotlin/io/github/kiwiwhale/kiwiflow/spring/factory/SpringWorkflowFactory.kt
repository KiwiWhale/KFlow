package io.github.kiwiwhale.kiwiflow.spring.factory

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiFlowWorkflow
import org.springframework.aop.support.AopUtils

/**
 * Creates KiwiFlow workflows from Spring-managed beans.
 *
 * Resolves the underlying target class of Spring AOP proxies
 * and delegates annotation inspection and workflow construction
 * to [AnnotatedWorkflowFactory].
 *
 * @see AnnotatedWorkflowFactory
 */
class SpringWorkflowFactory {

    /**
     * Creates a workflow from an annotated Spring bean.
     *
     * @param instance Spring-managed bean instance.
     * @param annotation Workflow annotation to use.
     * @return Executable KiwiFlow workflow.
     */
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
