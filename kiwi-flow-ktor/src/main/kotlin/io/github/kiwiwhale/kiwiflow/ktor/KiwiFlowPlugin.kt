package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.ktor.config.KiwiFlowConfig
import io.github.kiwiwhale.kiwiflow.ktor.registry.WorkflowRegistry
import io.ktor.server.application.*

val KiwiFlowPlugin = createApplicationPlugin(
    name = "KiwiFlowPlugin",
    createConfiguration = ::KiwiFlowConfig
) {
    val registry = WorkflowRegistry()

    pluginConfig.registeredWorkflows().forEach { (name, workflow) ->
        registry.register(name, workflow)
    }

    application.attributes.put(
        KiwiFlowApplicationKey,
        KiwiFlowApplication(registry)
    )
}