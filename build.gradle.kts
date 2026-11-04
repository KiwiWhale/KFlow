import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka)
}

group = "io.github.kiwiwhale.kiwiflow"
version = "1.1.0"

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
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()
                }
            }
        }
    }
}
