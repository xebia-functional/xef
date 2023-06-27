@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    java
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(projects.xefCore)
    implementation(projects.xefJava)
    implementation(projects.xefSql)
    implementation(projects.xefTokenizer)
}

tasks.withType<Test>().configureEach {
    useJUnit()
}