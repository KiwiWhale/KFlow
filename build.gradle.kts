plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "io.github.mrjimin.kflow"
version = "1.0-SNAPSHOT"

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

subprojects {
    pluginManager.apply("java-library")
    pluginManager.apply("org.jetbrains.kotlin.jvm")
}