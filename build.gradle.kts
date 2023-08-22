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
  alias(libs.plugins.avail.plugin) apply false
}

allprojects {
  group = property("project.group").toString()
}

fun isMultiplatformModule(project: Project): Boolean {
  val kotlinPluginId = "libs.plugins.kotlin.multiplatform"
  return project.buildFile.readText().contains(kotlinPluginId)
}

val multiPlatformModules = project.subprojects.filter { isMultiplatformModule(it) }.map { it.name }

enum class ModuleType {
  MULTIPLATFORM,
  SINGLEPLATFORM
}

fun Project.configureBuildAndTestTask(
  taskName: String,
  moduleType: ModuleType,
  multiPlatformModules: List<String>
) {
  val platform: String by extra

  tasks.register(taskName) {
    doLast {
      project.exec {
        val gradleCommand = getGradleCommand(platform)
        commandLine(gradleCommand, "spotlessCheck")
        when (moduleType) {
          ModuleType.MULTIPLATFORM -> {
            multiPlatformModules.forEach { module ->
              commandLine(gradleCommand, ":$module:${platform}Test")
            }
          }
          ModuleType.SINGLEPLATFORM -> {
            commandLine(gradleCommand, "build", *buildExcludeOptions(multiPlatformModules))
          }
        }
      }
    }
  }
}

fun Project.buildExcludeOptions(modules: List<String>): Array<String> {
  return modules.flatMap { listOf("-x", ":$it:build") }.toTypedArray()
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
  ModuleType.MULTIPLATFORM,
  multiPlatformModules
)

configureBuildAndTestTask(
  "buildAndTestSinglep",
  ModuleType.SINGLEPLATFORM,
  multiPlatformModules
)
