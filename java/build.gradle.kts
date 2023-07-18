@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    `java-library`
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.spotless)
}

dependencies {
    api(projects.xefCore)
    api(projects.xefOpenai)
    api(projects.xefPdf)
    api(projects.xefSql)
    api(libs.jdbc.mysql.connector)
    api(libs.jackson)
    api(libs.jackson.schema)
    api(libs.jackson.schema.jakarta)
    api(libs.jakarta.validation)
}

tasks.withType<Test>().configureEach {
    useJUnit()
}
