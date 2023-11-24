@file:Suppress("DSL_SCOPE_VIOLATION")

import org.jetbrains.dokka.gradle.DokkaTask

repositories { mavenCentral() }

plugins {
    base
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.dokka)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.detekt)
}

dependencies { detektPlugins(project(":detekt-rules")) }

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain { languageVersion = JavaLanguageVersion.of(11) }
}

detekt {
    toolVersion = "1.23.1"
    source.setFrom(files("src/commonMain/kotlin", "src/jvmMain/kotlin"))
    config.setFrom("../config/detekt/detekt.yml")
    autoCorrect = true
}

kotlin {
    jvm {
        compilations {
            val integrationTest by compilations.creating {
                // Create a test task to run the tests produced by this compilation:
                tasks.register<Test>("integrationTest") {
                    description = "Run the integration tests"
                    group = "verification"
                    classpath = compileDependencyFiles + runtimeDependencyFiles + output.allOutputs
                    testClassesDirs = output.classesDirs
                    testLogging { events("passed") }
                }
            }
            val test by compilations.getting
            integrationTest.associateWith(test)
        }
    }
    js(IR) {
        browser()
        nodejs()
    }
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    sourceSets {
        all { languageSettings.optIn("kotlin.ExperimentalStdlibApi") }
        val commonMain by getting {
            dependencies {
                implementation(projects.xefCore)
                implementation(libs.openai.client)
                implementation(libs.klogging)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotest.property)
                implementation(libs.kotest.framework)
                implementation(libs.kotest.assertions)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.logback)
                api(libs.ktor.client.cio)
            }
        }
        val jvmTest by getting { dependencies { implementation(libs.kotest.junit5) } }
        val jsMain by getting { dependencies { api(libs.ktor.client.js) } }
        val linuxX64Main by getting { dependencies { api(libs.ktor.client.cio) } }
        val macosX64Main by getting { dependencies { api(libs.ktor.client.cio) } }
        val macosArm64Main by getting { dependencies { api(libs.ktor.client.cio) } }
        val mingwX64Main by getting { dependencies { api(libs.ktor.client.winhttp) } }
        val linuxX64Test by getting
        val macosX64Test by getting
        val macosArm64Test by getting
        val mingwX64Test by getting
        create("nativeMain") {
            dependsOn(commonMain)
            linuxX64Main.dependsOn(this)
            macosX64Main.dependsOn(this)
            macosArm64Main.dependsOn(this)
            mingwX64Main.dependsOn(this)
        }
        create("nativeTest") {
            dependsOn(commonTest)
            linuxX64Test.dependsOn(this)
            macosX64Test.dependsOn(this)
            macosArm64Test.dependsOn(this)
            mingwX64Test.dependsOn(this)
        }
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle()
    }
}

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        dependsOn(":detekt-rules:assemble")
        autoCorrect = true
    }
    named("detektJvmMain") {
        dependsOn(":detekt-rules:assemble")
        getByName("build").dependsOn(this)
    }
    named("detekt") {
        dependsOn(":detekt-rules:assemble")
        getByName("build").dependsOn(this)
    }
    withType<Test>().configureEach {
        maxParallelForks = Runtime.getRuntime().availableProcessors()
        useJUnitPlatform()
        testLogging {
            setExceptionFormat("full")
            setEvents(listOf("passed", "skipped", "failed", "standardOut", "standardError"))
        }
    }
    withType<DokkaTask>().configureEach {
        kotlin.sourceSets.forEach { kotlinSourceSet ->
            dokkaSourceSets.named(kotlinSourceSet.name) {
                perPackageOption {
                    matchingRegex.set(".*\\.internal.*")
                    suppress.set(true)
                }
                skipDeprecated.set(true)
                reportUndocumented.set(false)
                val baseUrl = checkNotNull(project.properties["pom.smc.url"]?.toString())
                kotlinSourceSet.kotlin.srcDirs.filter { it.exists() }.forEach { srcDir ->
                    sourceLink {
                        localDirectory.set(srcDir)
                        remoteUrl.set(uri("$baseUrl/blob/main/${srcDir.relativeTo(rootProject.rootDir)}").toURL())
                        remoteLineSuffix.set("#L")
                    }
                }
            }
        }
    }
}

tasks.withType<AbstractPublishToMaven> { dependsOn(tasks.withType<Sign>()) }
