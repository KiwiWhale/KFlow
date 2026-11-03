package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.ktor.config.KiwiFlowConfig
import io.github.kiwiwhale.kiwiflow.ktor.registry.WorkflowRegistry
import io.ktor.server.application.createApplicationPlugin

/**
 * Ktor application plugin for integrating KiwiFlow.
 *
 * Registers configured workflows and exposes them through
 * [KiwiFlowApplication].
 *
 * All configured workflows are registered before the application
 * can access the installed KiwiFlow integration.
 *
 * @see KiwiFlowConfig
 * @see KiwiFlowApplication
 * @see WorkflowRegistry
 */
val KiwiFlowPlugin = createApplicationPlugin(
    name = "KiwiFlowPlugin",
    createConfiguration = ::KiwiFlowConfig
) {
    val registry = WorkflowRegistry()

    registry.registerAll(
        pluginConfig.registeredWorkflows()
    )

    application.attributes.put(
        KiwiFlowApplicationKey,
        KiwiFlowApplication(registry)
    )
}
