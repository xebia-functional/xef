plugins {
    id(libs.plugins.kotlin.multiplatform.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
    alias(libs.plugins.spotless)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.detekt)
}

dependencies { detektPlugins(project(":detekt-rules")) }

detekt {
    toolVersion = "1.23.1"
    source = files("src/commonMain/kotlin", "src/jvmMain/kotlin")
    config.setFrom("../../config/detekt/detekt.yml")
    autoCorrect = true
}

repositories { mavenCentral() }

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain { languageVersion = JavaLanguageVersion.of(11) }
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.xefCore)
                implementation(libs.bundles.ktor.client)
                implementation(libs.uuid)
                implementation(libs.kotlinx.datetime)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.logback)
                api(libs.ktor.client.cio)
            }
        }
        val jsMain by getting {
            dependencies {
                api(libs.ktor.client.js)
            }
        }
        val linuxX64Main by getting {
            dependencies {
                api(libs.ktor.client.cio)
            }
        }
        val macosX64Main by getting {
            dependencies {
                api(libs.ktor.client.cio)
            }
        }
        val macosArm64Main by getting {
            dependencies {
                api(libs.ktor.client.cio)
            }
        }
        val mingwX64Main by getting {
            dependencies {
                api(libs.ktor.client.winhttp)
            }
        }
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle().configure { it.setRemoveUnusedImport(true) }
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
	withType<AbstractPublishToMaven> { dependsOn(withType<Sign>()) }
}
