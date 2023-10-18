plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

dependencies { detektPlugins(project(":detekt-rules")) }

repositories { mavenCentral() }

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain { languageVersion = JavaLanguageVersion.of(11) }
}

detekt {
    toolVersion = "1.23.1"
    source = files("src/main/kotlin")
    config.setFrom("../../config/detekt/detekt.yml")
    autoCorrect = true
}

dependencies {
    implementation(projects.xefCore)
    implementation(libs.opentelemetry.api)
    implementation(libs.opentelemetry.exporter.logging)
    implementation(libs.opentelemetry.sdk)
    implementation(libs.opentelemetry.semconv)
    implementation(libs.opentelemetry.extension.kotlin)
    implementation(libs.opentelemetry.exporter.otlp)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.framework)
    testImplementation(libs.kotest.assertions)
    testRuntimeOnly(libs.kotest.junit5)
}

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle().configure {
            it.setRemoveUnusedImport(true)
        }
    }
}

tasks {
    withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        dependsOn(":detekt-rules:assemble")
        autoCorrect = true
    }
    named("detekt") {
        dependsOn(":detekt-rules:assemble")
        getByName("build").dependsOn(this)
    }

    withType<AbstractPublishToMaven> { dependsOn(withType<Sign>()) }
}
