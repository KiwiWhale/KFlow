package io.github.mrjimin.kflow.spring.factory

import io.github.mrjimin.kflow.annotation.factory.AnnotatedWorkflowFactory
import io.github.mrjimin.kflow.workflow.Workflow

class SpringWorkflowFactory {

    @Suppress("UNCHECKED_CAST")
    fun <I, O> create(instance: Any): Workflow<I, O> {
        return AnnotatedWorkflowFactory.fromInstance<I, O>(instance)
    }
}