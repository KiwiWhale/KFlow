dependencies {
    implementation(project(":kiwi-flow-core"))
    implementation(project(":kiwi-flow-annotations"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}