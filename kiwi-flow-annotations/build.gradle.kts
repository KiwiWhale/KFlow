dependencies {
    api(project(":kiwi-flow-core"))

    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}