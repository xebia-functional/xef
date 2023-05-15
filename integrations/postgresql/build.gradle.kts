plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

dependencies {
    implementation(projects.xefCore)
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
