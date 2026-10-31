dependencies {
    api(libs.kotlinx.coroutines.core)

    implementation(kotlin("reflect"))

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}