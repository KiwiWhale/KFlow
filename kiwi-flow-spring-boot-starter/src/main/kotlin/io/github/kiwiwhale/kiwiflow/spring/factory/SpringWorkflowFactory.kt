package io.github.kiwiwhale.kiwiflow.spring.factory

import io.github.kiwiwhale.kiwiflow.annotation.Workflow
import io.github.kiwiwhale.kiwiflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.kiwiwhale.kiwiflow.core.workflow.KiwiWorkflow
import org.springframework.aop.support.AopUtils

/**
 * Creates [KiwiWorkflow] instances from Spring-managed beans.
 *
 * Resolves the underlying target class of Spring AOP proxies
 * and delegates workflow inspection and construction to
 * [AnnotatedWorkflowFactory].
 *
 * Spring-specific reflection is isolated within this factory,
 * allowing the annotation module to remain independent
 * of Spring.
 *
 * @see AnnotatedWorkflowFactory
 */
class SpringWorkflowFactory {

    /**
     * Creates a workflow from an annotated Spring bean.
     *
     * The target class is resolved from the Spring-managed bean
     * to ensure that annotations are inspected on the actual
     * workflow implementation rather than its proxy.
     *
     * @param instance Spring-managed workflow bean.
     * @param annotation Workflow annotation to use.
     * @return Executable workflow.
     */
    fun create(
        instance: Any,
        annotation: Workflow
    ): KiwiWorkflow<Any?, Any?> {
        val targetClass = AopUtils.getTargetClass(instance).kotlin

        return AnnotatedWorkflowFactory.fromInstance(
            instance = instance,
            annotation = annotation,
            targetClass = targetClass
        )
    }
}