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

val multiPlatformModules = listOf(
  "xef-core",
  "xef-filesystem",
  "xef-tokenizer"
)

fun Project.configureBuildAndTestTask(
  taskName: String,
  multiPlatformModules: List<String>,
  singlePlatformCommand: String
) {
  val platform: String by extra

  tasks.register(taskName) {
    doLast {
      project.exec {
        val gradleCommand = getGradleCommand(platform)
        commandLine(gradleCommand, "spotlessCheck")
        if (singlePlatformCommand.isEmpty()) {
          multiPlatformModules.forEach { module ->
            commandLine(gradleCommand, ":$module:${platform}Test")
          }
        } else {
          val excludedModules = multiPlatformModules.map { ":$it:build" }
          commandLine(gradleCommand, singlePlatformCommand, "-x", excludedModules.joinToString(" -x "))
        }
      }
    }
  }
}

fun getGradleCommand(platform: String): String {
  return if (platform == "mingwX64") {
    "gradlew.bat"
  } else {
    "./gradlew"
  }
}

configureBuildAndTestTask(
  "buildAndTestMultip",
  multiPlatformModules,
  ""
)

configureBuildAndTestTask(
  "buildAndTestSinglep",
  multiPlatformModules,
  "build"
)
