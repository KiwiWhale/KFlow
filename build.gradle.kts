
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka)
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

group = "io.github.kiwiwhale.kiwiflow"
version = "1.2.0"

val mavenUsername = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val mavenPassword = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

dependencies {
    dokka(project(":kiwi-flow-core"))
    dokka(project(":kiwi-flow-annotation"))
    dokka(project(":kiwi-flow-ktor"))
    dokka(project(":kiwi-flow-spring-boot-starter"))
}

dokka {
    dokkaPublications.html {
        moduleName.set("KiwiFlow")

        pluginsConfiguration.html {
            footerMessage.set("(c) KiwiWhale")
        }
    }
}

subprojects {
    pluginManager.apply("java-library")
    pluginManager.apply("org.jetbrains.kotlin.jvm")

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain(25)
    }

    repositories {
        mavenCentral()
        maven("https://repo.mrjimin.xyz/releases")
        maven("https://repo.mrjimin.xyz/snapshots")

        // maven("https://jitpack.io")
    }

    dependencies {
        "testImplementation"(kotlin("test"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    if (name != "kiwi-flow-example") {
        pluginManager.apply("maven-publish")
        pluginManager.apply("org.jetbrains.dokka")

        extensions.configure<PublishingExtension> {
            repositories {
                maven {
                    name = "mrjiminRepository"

                    url = uri(
                        if (project.version.toString().endsWith("-SNAPSHOT"))
                            "https://repo.mrjimin.xyz/snapshots"
                        else
                            "https://repo.mrjimin.xyz/releases"
                    )

                    credentials {
                        username = mavenUsername
                        password = mavenPassword
                    }

                    authentication {
                        create<BasicAuthentication>("basic")
                    }
                }
            }

            publications {
                create<MavenPublication>("maven") {
                    groupId = "io.github.kiwiwhale"
                    artifactId = project.name
                    version = project.version.toString()

                    from(components["java"])
                }
            }
        }
    }
}
