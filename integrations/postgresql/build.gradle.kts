plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.detekt)
}

dependencies { detektPlugins(project(":detekt-rules")) }

repositories {
    mavenCentral()
    mavenLocal()
}

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
    implementation(libs.flyway.core)
    implementation(libs.uuid)
    implementation(libs.hikari)
    implementation(libs.postgresql)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.framework)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.kotest.junit5)
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
