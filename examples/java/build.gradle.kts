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

val ENABLE_PREVIEW = "--enable-preview"

tasks.withType<JavaCompile> {
    options.compilerArgs.add(ENABLE_PREVIEW)
}
tasks.test {
    useJUnitPlatform()
    jvmArgs(ENABLE_PREVIEW)
}
