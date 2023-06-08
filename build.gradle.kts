@file:Suppress("DSL_SCOPE_VIOLATION")

plugins {
  base
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlinx.serialization)
  alias(libs.plugins.spotless)
  alias(libs.plugins.dokka)
  alias(libs.plugins.arrow.gradle.nexus)
  alias(libs.plugins.arrow.gradle.publish) apply false
  alias(libs.plugins.semver.gradle)
}

allprojects {
  group = property("project.group").toString()
}

tasks.register("buildAndTestMultip") {
  val platform: String by project.extensions.extraProperties
  val gradleCommand: String by project.extensions.extraProperties

  doLast {
    project.exec {
      commandLine(gradleCommand,
        "spotlessCheck",
        ":xef-core:${platform}Test",
        ":xef-filesystem:${platform}Test",
        ":xef-tokenizer:${platform}Test"
      )
    }
  }
}

tasks.register("buildAndTestSinglep") {
  val gradleCommand: String by project.extensions.extraProperties

  doLast {
    project.exec {
      commandLine(gradleCommand,
        "spotlessCheck",
        ":xef-lucene:build",
        ":xef-pdf:build",
        ":xef-postgresql:build",
        ":xef-sql:build",
        ":xef-kotlin-examples:build",
        ":kotlin-loom:build",
        ":xef-scala-examples:build",
        ":xef-scala:build",
        ":xef-scala-cats:build"
      )
    }
  }
}
