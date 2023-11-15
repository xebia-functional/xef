import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

internal val Project.isJavaPlatform: Boolean
  get() = pluginManager.hasPlugin("org.gradle.java-platform")

internal val Project.isKotlinJvm: Boolean
  get() = pluginManager.hasPlugin("org.jetbrains.kotlin.jvm")

internal val Project.isKotlinMultiplatform: Boolean
  get() = pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")

internal val Project.isScala: Boolean
  get() = pluginManager.hasPlugin("org.gradle.scala")

enum class HostTarget {
  Linux, MacOS, Windows
}

internal fun Project.configValue(propertyName: String, envVarName: String): String? {
  val property: String? = project.properties[propertyName]?.toString()
  val envVar: String? = System.getenv(envVarName)
  val configValue = property ?: envVar
  return configValue.also {
    if (configValue.isNullOrBlank()) {
      errorMessage("$propertyName Gradle property and $envVarName environment variable are missing")
    }
  }
}

internal val KotlinTarget.hostTarget: HostTarget?
  get() =
    when (platformType) {
      KotlinPlatformType.jvm -> HostTarget.Linux
      KotlinPlatformType.common -> null
      KotlinPlatformType.js -> HostTarget.Linux
      KotlinPlatformType.androidJvm -> HostTarget.Linux
      KotlinPlatformType.native ->
        when {
          name.startsWith("ios") -> HostTarget.MacOS
          name.startsWith("linux") -> HostTarget.Linux
          name.startsWith("macos") -> HostTarget.MacOS
          name.startsWith("mingw") -> HostTarget.Windows
          name.startsWith("tvos") -> HostTarget.MacOS
          name.startsWith("watchos") -> HostTarget.MacOS
          else -> error("Unsupported native target: $name")
        }

      KotlinPlatformType.wasm -> HostTarget.Linux
    }

internal fun KotlinTarget.isCompilationAllowed(): Boolean {
  if (name == KotlinMultiplatformPlugin.METADATA_TARGET_NAME)
    return true

  val os = OperatingSystem.current()

  return when (hostTarget) {
    HostTarget.Linux -> os.isLinux
    HostTarget.MacOS -> os.isMacOsX
    HostTarget.Windows -> os.isWindows
    null -> true
  }
}

internal fun KotlinTarget.disableCompilationIfNeeded() {
  if (!isCompilationAllowed()) disableCompilations()
}

internal fun KotlinTarget.disableCompilations() {
  project.logger.lifecycle("Compilation disabled for target $name in ${project.name}")

  compilations.configureEach {
    compileTaskProvider.get().enabled = false
  }

  project.tasks.withType<JavaCompile> {
    enabled = false
  }

  project.tasks.withType<Test> {
    enabled = false
  }

  mavenPublication {
    val publicationToDisable = this
    project.tasks.withType<AbstractPublishToMaven>()
      .all { onlyIf { publication != publicationToDisable } }
    project.tasks.withType<GenerateModuleMetadata>()
      .all { onlyIf { publication.get() != publicationToDisable } }
  }

  (this as? KotlinNativeTarget)?.let {
    it.compilations.all {
      cinterops.all { project.tasks[interopProcessingTaskName].enabled = false }
      project.tasks[processResourcesTaskName].enabled = false
    }
    binaries.all { linkTask.enabled = false }
  }
}

internal fun Project.disableSinglePlatformCompilations() {
  tasks.withType<AbstractCompile> {
    enabled = false
  }

  tasks.withType<Test> {
    enabled = false
  }

  tasks.withType<AbstractPublishToMaven> {
    enabled = false
  }

  tasks.withType<GenerateModuleMetadata> {
    enabled = false
  }
}

internal fun MavenPublication.pomConfiguration(project: Project) {
  pom {
    name.set(project.properties["pom.name"]?.toString())
    description.set(project.properties["pom.description"]?.toString())
    url.set(project.properties["pom.url"]?.toString())
    licenses {
      license {
        name.set(project.properties["pom.license.name"]?.toString())
        url.set(project.properties["pom.license.url"]?.toString())
      }
    }
    developers {
      developer {
        id.set(project.properties["pom.developer.id"].toString())
        name.set(project.properties["pom.developer.name"].toString())
      }
    }
    scm {
      url.set(project.properties["pom.smc.url"].toString())
      connection.set(project.properties["pom.smc.connection"].toString())
      developerConnection.set(project.properties["pom.smc.developerConnection"].toString())
    }
  }
}

internal fun Project.errorMessage(message: String) = logger.lifecycle("$YELLOW$message$RESET")

private const val RESET = "\u001B[0m"
private const val YELLOW = "\u001B[0;33m"
