dependencies {
    implementation(project(":kiwi-flow-core"))
    implementation(project(":kiwi-flow-annotation"))

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}
