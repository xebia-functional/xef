@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    scala
    alias(libs.plugins.scala.multiversion)
}

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation(libs.ciris.core)
    implementation(libs.ciris.refined)
    implementation(libs.ciris.http4s)
    implementation(libs.http4s.dsl)
    implementation(libs.http4s.client)
    implementation(libs.http4s.circe)
    implementation(libs.http4s.emberClient)
    implementation(libs.doobie.core)
    implementation(libs.doobie.postgres)
    implementation(libs.doobie.hikari)
    implementation(libs.doobie.munit)
    implementation(libs.circe)
    implementation(libs.cats.effect)
    implementation(libs.logger)
    implementation(libs.openai)
    implementation(libs.cats.retry)
    implementation(libs.memeid)
    testImplementation(libs.munit.core)
    testImplementation(libs.munit.cats.effect)
    testImplementation(libs.scala.testcontainers.munit)
    testImplementation(libs.scala.testcontainers.postgresql)
}

tasks.withType<Test>().configureEach {
    useJUnit()
}