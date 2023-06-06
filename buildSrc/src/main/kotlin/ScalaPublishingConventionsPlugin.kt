import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension

class ScalaPublishingConventionsPlugin : Plugin<Project> {
  override fun apply(project: Project): Unit = project.run {
    val scaladocJarTask: TaskProvider<Jar> = tasks.register<Jar>("scaladocJar") {
      group = BasePlugin.BUILD_GROUP
      tasks.findByName("scaladoc")?.let { dependsOn(it) }
      archiveClassifier.set("javadoc")
      from("$buildDir/docs/scaladoc")
    }

    extensions.configure(PublishingExtension::class.java) {
      publications {
        register("maven", MavenPublication::class) {
          val scala3Suffix = "_3"
          from(components["java"])
          artifact(scaladocJarTask)

          extensions.findByType(BasePluginExtension::class.java)?.let {
            artifactId = it.archivesName.get() + scala3Suffix
          }

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

    extensions.configure(SigningExtension::class.java) {
      val isLocal = gradle.startParameter.taskNames.any { it.contains("publishToMavenLocal", ignoreCase = true) }
      val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
      val signingKey: String? = System.getenv("SIGNING_KEY")
      val signingPassphrase: String? = System.getenv("SIGNING_KEY_PASSPHRASE")

      isRequired = !isLocal
      useGpgCmd()
      useInMemoryPgpKeys(signingKeyId, signingKey, signingPassphrase)
      extensions.findByType(PublishingExtension::class.java)?.let { sign(it.publications) }
    }
  }
}
