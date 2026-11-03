plugins {
    kotlin("plugin.spring")
}

dependencies {
    api(project(":kiwi-flow-core"))
    api(project(":kiwi-flow-annotation"))

    api(libs.spring.boot.autoconfigure)

    testImplementation(libs.spring.boot.starter.test)
}