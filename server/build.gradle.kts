plugins {
    id(libs.plugins.kotlin.jvm.get().pluginId)
    id(libs.plugins.kotlinx.serialization.get().pluginId)
    alias(libs.plugins.node.gradle)
    alias(libs.plugins.arrow.gradle.publish)
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain { languageVersion = JavaLanguageVersion.of(11) }
}

node { nodeProjectDir.set(file("${project.projectDir}/web")) }

dependencies {
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.java.time)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)
    implementation(libs.flyway.core)
    implementation(libs.hikari)
    implementation(libs.klogging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.suspendApp.core)
    implementation(libs.suspendApp.ktor)
    implementation(libs.uuid)
    implementation(projects.xefCore)
    implementation(projects.xefPostgresql)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.framework)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.kotest.junit5)
    implementation(libs.logback)
    implementation(libs.klogging)
}

spotless {
    kotlin {
        target("**/*.kt")
        ktfmt().googleStyle().configure { it.setRemoveUnusedImport(true) }
    }
}

task<JavaExec>("web-app") {
    dependsOn("npm_run_build")
    group = "Execution"
    description = "xef-server web application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.xebia.functional.xef.server.WebApp")
}

task<JavaExec>("server") {
    dependsOn("compileKotlin")
    group = "Execution"
    description = "xef-server server application"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.xebia.functional.xef.server.Server")
}

tasks.named<Test>("test") { useJUnitPlatform() }

tasks.withType<AbstractPublishToMaven> { dependsOn(tasks.withType<Sign>()) }
