plugins {
    kotlin("plugin.spring")
}

dependencies {
    api(project(":kflow-core"))
    api(project(":kflow-annotations"))

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