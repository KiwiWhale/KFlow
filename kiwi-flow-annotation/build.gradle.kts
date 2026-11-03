dependencies {
    api(project(":kiwi-flow-core"))

    implementation(kotlin("reflect"))

    testImplementation(libs.kotlinx.coroutines.test)
}
