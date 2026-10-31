package io.github.mrjimin.kflow.ktor

import io.github.mrjimin.kflow.ktor.config.KFlowConfig
import io.github.mrjimin.kflow.ktor.registry.WorkflowRegistry
import io.ktor.server.application.*

val KFlowPlugin = createApplicationPlugin(
    name = "KFlowPlugin",
    createConfiguration = ::KFlowConfig
) {
    val registry = WorkflowRegistry()

    pluginConfig.registeredWorkflows().forEach { (name, workflow) ->
        registry.register(name, workflow)
    }

    application.attributes.put(
        KFlowApplicationKey,
        KFlowApplication(registry)
    )
}