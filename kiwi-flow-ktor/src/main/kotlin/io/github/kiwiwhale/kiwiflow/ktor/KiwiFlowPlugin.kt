package io.github.kiwiwhale.kiwiflow.ktor

import io.github.kiwiwhale.kiwiflow.ktor.config.KiwiFlowConfig
import io.ktor.server.application.createApplicationPlugin

/**
 * Ktor integration plugin for KiwiFlow.
 *
 * Core owns workflow registration.
 * Ktor owns application lifecycle integration.
 */
val KiwiFlowPlugin = createApplicationPlugin(
    name = "KiwiFlowPlugin",
    createConfiguration = ::KiwiFlowConfig
) {
    application.attributes.put(
        KiwiFlowApplicationKey,
        KiwiFlowApplication(pluginConfig.registry)
    )
}
