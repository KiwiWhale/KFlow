dependencies {
    implementation(project(":kflow-core"))
    implementation(project(":kflow-annotations"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}