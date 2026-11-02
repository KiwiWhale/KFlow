plugins {
    kotlin("plugin.spring")
}

dependencies {
    api(project(":kiwi-flow-core"))
    api(project(":kiwi-flow-annotations"))

    api(libs.spring.boot.autoconfigure)

    testImplementation(kotlin("test"))
    testImplementation(libs.spring.boot.starter.test)
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}