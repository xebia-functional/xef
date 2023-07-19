@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    `java-library`
    `maven-publish`
    signing
    `xef-java-publishing-conventions`
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.spotless)
}

dependencies {
    api(projects.xefCore)
    api(projects.xefOpenai)
    api(projects.xefPdf)
    api(projects.xefGpt4all)
    api(libs.jackson)
    api(libs.jackson.schema)
    api(libs.jackson.schema.jakarta)
    api(libs.jakarta.validation)
    api(libs.kotlinx.coroutines.reactive)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn(tasks.withType<Sign>())
}
