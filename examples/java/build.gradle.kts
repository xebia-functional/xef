@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    java
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(projects.xefJava)
    implementation(projects.xefReasoning)
    implementation(projects.xefGpt4all)
}

tasks.withType<Test>().configureEach {
    useJUnit()
}
