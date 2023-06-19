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
    implementation(libs.scala.lang)
    implementation(libs.cats.effect)
    testImplementation(libs.munit.core)
    testImplementation(libs.munit.cats.effect)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
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
