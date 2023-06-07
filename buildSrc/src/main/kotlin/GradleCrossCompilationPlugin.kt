import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests

class GradleCrossCompilationPlugin: Plugin<Project> {
  override fun apply(project: Project): Unit = project.run {
    val kotlinMultiplatformExtension = project.extensions.findByType(KotlinMultiplatformExtension::class.java)

    afterEvaluate {
      kotlinMultiplatformExtension?.run {
        val os = OperatingSystem.current()

        val excludedNativeTargets = when {
          os.isLinux -> targets.filter { target -> !target.name.contains("linux") }
          os.isMacOsX -> targets.filter { target -> target.name.contains("linux") || target.name.contains("mingw") }
          os.isWindows -> targets.filter { target -> !target.name.contains("mingw") }
          else -> error("Unknown Operative System: ${os.name}")
        }.mapNotNull { target -> target as? KotlinNativeTargetWithHostTests }

        logger.lifecycle("Compilation disabled for the following targets:")
        excludedNativeTargets.forEach { logger.lifecycle(" - ${it.name}") }

        configure(excludedNativeTargets) {
          compilations.all {
            compilations.all {
              cinterops.all { tasks[interopProcessingTaskName].enabled = false }
              compileTaskProvider.get().enabled = false
              tasks[processResourcesTaskName].enabled = false
            }
            binaries.all { linkTask.enabled = false }

            mavenPublication {
              val publicationToDisable = this
              tasks.withType<AbstractPublishToMaven>()
                .all { onlyIf { publication != publicationToDisable } }
              tasks.withType<GenerateModuleMetadata>()
                .all { onlyIf { publication.get() != publicationToDisable } }
            }
          }
        }
      }
    }
  }
}
