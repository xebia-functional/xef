@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    scala
    alias(libs.plugins.scala.multiversion)
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(projects.xefCore)
    implementation(projects.kotlinLoom)
    implementation(libs.kotlinx.coroutines)
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
    implementation("com.47deg:unwrapped_3:0.0.0+186-c4a495f7-SNAPSHOT")
    testImplementation(libs.munit.core)
    testImplementation(libs.munit.cats.effect)
    testImplementation(libs.scala.testcontainers.munit)
    testImplementation(libs.scala.testcontainers.postgresql)
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

spotless {
    scala {
        scalafmt("3.7.1").configFile(".scalafmt.conf").scalaMajorVersion("2.13")
    }
}