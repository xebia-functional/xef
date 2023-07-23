import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

internal fun Project.configValue(propertyName: String, environmentVariableName: String): String? {
  val property: String? = project.properties[propertyName]?.toString()
  val environmentVariable: String? = System.getenv(environmentVariableName)

  val configValue = property ?: environmentVariable

  return configValue.also {
    if (configValue.isNullOrBlank()) {
      errorMessage(
        "$propertyName Gradle property and " +
          "$environmentVariableName environment variable are missing",
      )
    }
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
