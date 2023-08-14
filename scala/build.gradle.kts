@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
    scala
    `maven-publish`
    signing
    alias(libs.plugins.semver.gradle)
    alias(libs.plugins.spotless)
    `xef-scala-publishing-conventions`
}

dependencies {
    implementation(projects.xefCore)
    implementation(projects.xefOpenai)
    implementation(projects.kotlinLoom)
    implementation(libs.kotlinx.coroutines.reactive)

    // TODO split to separate Scala library
    implementation(projects.xefPdf)

    implementation(libs.circe.parser)
    implementation(libs.circe)
    implementation(libs.scala.lang)
    implementation(libs.logback)
    testImplementation(libs.munit.core)
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
    withSourcesJar()
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

tasks.withType<ScalaCompile> {
    scalaCompileOptions.additionalParameters = listOf("-Wunused:all", "-Wvalue-discard")
}

spotless {
    scala {
        scalafmt("3.7.3").configFile(".scalafmt.conf").scalaMajorVersion("2.13")
    }
}
