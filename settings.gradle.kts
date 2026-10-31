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

rootProject.name = "KFlow"

include("kflow-core")
include("kflow-annotations")
include("kflow-example")
include("kflow-spring-boot-starter")
include("kflow-ktor")