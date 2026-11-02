pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("plugin.spring") version "2.4.20"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kiwi-flow"

include("kiwi-flow-core")
include("kiwi-flow-annotations")
include("kiwi-flow-example")
include("kiwi-flow-spring-boot-starter")
include("kiwi-flow-ktor")