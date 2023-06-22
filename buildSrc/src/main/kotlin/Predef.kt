import org.gradle.api.Project

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

internal fun Project.errorMessage(message: String) = logger.lifecycle("$YELLOW$message$RESET")

private const val RESET = "\u001B[0m"
private const val YELLOW = "\u001B[0;33m"
