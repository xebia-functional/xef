package com.xebia.functional.xef.io


data class ExecuteCommandOptions(
  val directory: String,
  val abortOnError: Boolean,
  val redirectStderr: Boolean,
  val trim: Boolean
)

sealed class Platform(open val archName: String) {
  data class LINUX(override val archName: String) : Platform(archName)
  data class MACOS(override val archName: String) : Platform(archName)
  data class WINDOWS(override val archName: String) : Platform(archName)
}

interface CommandExecutor {

  suspend fun platform(): Platform

  suspend fun executeCommandAndCaptureOutput(
    command: List<String>,
    options: ExecuteCommandOptions
  ): String

  suspend fun pwd(options: ExecuteCommandOptions): String

  suspend fun findExecutable(executable: String): String

  companion object
}

expect val CommandExecutor.Companion.DEFAULT: CommandExecutor
