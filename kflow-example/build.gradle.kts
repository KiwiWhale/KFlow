dependencies {
    implementation(project(":kflow-core"))
    implementation(project(":kflow-annotations"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}
