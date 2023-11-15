import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.plugins.signing.SigningExtension

class JavaPublishingConventionsPlugin : Plugin<Project> {

  override fun apply(project: Project): Unit = project.run {
    val publishingExtension: PublishingExtension =
      extensions.findByType<PublishingExtension>()
        ?: throw IllegalStateException("The Maven Publish plugin is required to publish the build artifacts")

    val signingExtension: SigningExtension =
      extensions.findByType<SigningExtension>()
        ?: throw IllegalStateException("The Signing plugin is required to digitally sign the built artifacts")

    publishingExtension.run {
      publications {
        register<MavenPublication>("maven") {
          from(components["java"])
          pomConfiguration(project)
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
