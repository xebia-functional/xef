import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension

class ScalaPublishingConventionsPlugin : Plugin<Project> {
  override fun apply(project: Project): Unit = project.run {
    val scaladocJarTask: TaskProvider<Jar> = tasks.register<Jar>("scaladocJar") {
      group = BasePlugin.BUILD_GROUP
      tasks.findByName("scaladoc")?.let { dependsOn(it) }
        ?: errorMessage("The scaladoc task was not found. The Javadoc jar file won't contain any documentation")
      archiveClassifier.set("javadoc")
      from("$buildDir/docs/scaladoc")
    }

    val publishingExtension: PublishingExtension =
      extensions.findByType<PublishingExtension>()
        ?: throw IllegalStateException("The Maven Publish plugin is required to publish the build artifacts")

    val signingExtension: SigningExtension =
      extensions.findByType<SigningExtension>()
        ?: throw IllegalStateException("The Signing plugin is required to digitally sign the built artifacts")

    val basePluginExtension: BasePluginExtension =
      extensions.findByType<BasePluginExtension>()
        ?: throw IllegalStateException("The Base plugin is required to configure the name of artifacts")

    publishingExtension.run {
      publications {
        register<MavenPublication>("maven") {
          val scala3Suffix = "_3"

          artifactId = basePluginExtension.archivesName.get() + scala3Suffix
          from(components["java"])
          artifact(scaladocJarTask)

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
      }
    }

    signingExtension.run {
      val isLocal = gradle.startParameter.taskNames.any { it.contains("publishToMavenLocal", ignoreCase = true) }
      val signingKeyId: String? = configValue("signing.keyId", "SIGNING_KEY_ID")
      val signingKey: String? = configValue("signing.key", "SIGNING_KEY")
      val signingPassphrase: String? = configValue("signing.passphrase", "SIGNING_KEY_PASSPHRASE")

      isRequired = !isLocal
      useGpgCmd()
      useInMemoryPgpKeys(signingKeyId, signingKey, signingPassphrase)
      sign(publishingExtension.publications)
    }
  }
}
