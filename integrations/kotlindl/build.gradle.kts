plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
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
    ("org.jetbrains.kotlinx:kotlin-deeplearning-tensorflow:0.5.23")
    ("org.jetbrains.kotlinx:kotlin-deeplearning-onnx:0.5.23")
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn(tasks.withType<Sign>())
}
