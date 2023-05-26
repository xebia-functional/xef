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
    implementation(libs.circe.parser)
    implementation(libs.circe)
    implementation(libs.cats.effect)
    testImplementation(libs.munit.core)
    testImplementation(libs.munit.cats.effect)
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
}

tasks.withType<Test>().configureEach {
    useJUnit()
}

spotless {
    scala {
        scalafmt("3.7.1").configFile(".scalafmt.conf").scalaMajorVersion("2.13")
    }
}