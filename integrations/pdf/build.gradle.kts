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
    implementation(projects.xefCore)
    implementation(projects.xefTokenizer)
    implementation(libs.apache.pdf.box)
    implementation(libs.ktor.client.cio)
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn(tasks.withType<Sign>())
}
