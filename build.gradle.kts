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

fun Project.configureBuildAndTestTask(taskName: String, moduleType: ModulePlatformType) {
  val platform: String by extra
  val multiPlatformModules = project.subprojects.filter { isMultiPlatformModule(it) }.map { it.name }

  tasks.register(taskName) {
    val gradleCommand = getGradleCommand(platform)

    doFirst {
        project.exec {commandLine(gradleCommand, "spotlessCheck") }
    }

    doLast {
      when (moduleType) {
          ModulePlatformType.SINGLE -> {
              val excludedModules = includeOrNotModulesToCommand(multiPlatformModules, platform, false)
              println("modulesToExclude: ${excludedModules.toList()}")
              project.exec { commandLine(gradleCommand, "build", *excludedModules) }
          }

          ModulePlatformType.MULTI -> {
            val includedModules = includeOrNotModulesToCommand(multiPlatformModules, platform, true)
              println("modulesToInclude: ${includedModules.toList()}")
            project.exec { commandLine(gradleCommand, *includedModules) }
          }
      }
    }
  }
}

enum class ModulePlatformType { SINGLE, MULTI }

fun isMultiPlatformModule(project: Project): Boolean {
    val kotlinPluginId = "libs.plugins.kotlin.multiplatform"
    return project.buildFile.readText().contains(kotlinPluginId)
}

fun includeOrNotModulesToCommand(modules: List<String>, platform: String, include: Boolean): Array<String> {
    return modules.flatMap {
        when (include) {
            true -> listOf(":$it:${platform}Test")
            false -> listOf("-x", ":$it:build")
        }
    }.toTypedArray()
}

fun buildExcludeOptions(modules: List<String>): Array<String> {
  return modules.flatMap { module -> listOf("-x", ":$module:build") }.toTypedArray()
}

fun getGradleCommand(platform: String): String {
  return if (platform == "mingwX64") "gradlew.bat" else "./gradlew"
}

configureBuildAndTestTask("buildAndTestMultip", ModulePlatformType.MULTI)
configureBuildAndTestTask("buildAndTestSinglep", ModulePlatformType.SINGLE)
