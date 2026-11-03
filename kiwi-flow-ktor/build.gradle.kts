dependencies {
    api(project(":kiwi-flow-core"))

    api(platform(libs.ktor.bom))
    api(libs.ktor.server.core)

    testImplementation(libs.ktor.server.test.host)
}
