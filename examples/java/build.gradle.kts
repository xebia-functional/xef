@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    java
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(projects.xefJava)
    implementation(libs.kotlinx.coroutines.reactive)
}

tasks.withType<Test>().configureEach {
    useJUnit()
}