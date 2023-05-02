package com.xebia.functional.io


data class ExecuteCommandOptions(
  val directory: String,
  val abortOnError: Boolean,
  val redirectStderr: Boolean,
  val trim: Boolean
)

enum class Platform {
  LINUX, MACOS, WINDOWS,
}

interface CommandExecutor {
  suspend fun executeCommandAndCaptureOutput(
    command: List<String>,
    options: ExecuteCommandOptions
  ): String

  suspend fun pwd(options: ExecuteCommandOptions): String

  suspend fun findExecutable(executable: String): String

  companion object
}
