import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.compile.AbstractCompile
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class GradleCrossCompilationPlugin : Plugin<Project> {
  override fun apply(project: Project): Unit = project.run {
    when {
      isKotlinMultiplatform ->
        extensions.configure<KotlinMultiplatformExtension> {
          afterEvaluate {
            targets.configureEach {
              disableCompilationIfNeeded()
            }
          }
        }
      isKotlinJvm ->
        extensions.configure<KotlinJvmProjectExtension> {
          afterEvaluate {
            target.disableCompilationIfNeeded()
          }
        }
      isScala || isJavaPlatform -> {
        disableSinglePlatformCompilations()
      }
    }

  }

  private fun Project.singlePlatformSetup(platformEnabled: Boolean) {
    if (!platformEnabled) {
      logger.lifecycle("Compilation disabled for the module $name")

      tasks.withType<AbstractCompile> {
        enabled = false
      }
      tasks.withType<AbstractPublishToMaven> {
        enabled = false
      }
      tasks.withType<GenerateModuleMetadata> {
        enabled = false
      }
    }
  }
}
