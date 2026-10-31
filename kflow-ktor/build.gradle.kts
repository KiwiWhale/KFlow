repositories {
    mavenCentral()
}

dependencies {
    api(project(":kflow-core"))

    api(platform(libs.ktor.bom))
    api(libs.ktor.server.core)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}