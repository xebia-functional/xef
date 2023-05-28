package com.xebia.functional.xef.io

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import okio.FileSystem

actual val CommandExecutor.Companion.DEFAULT: CommandExecutor
  get() = JvmCommandExecutor

object JvmCommandExecutor : CommandExecutor {

  // TODO Remove RunBlocking
  override suspend fun platform(): Platform {
    val osName = System.getProperty("os.name").lowercase()
    val uname =
      try {
        executeCommandAndCaptureOutput(
          listOf("uname", "-m"),
          ExecuteCommandOptions(
            directory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString(),
            abortOnError = true,
            redirectStderr = true,
            trim = true,
          ),
        )
      } catch (e: Exception) {
        ""
      }

    return when {
      osName.startsWith("windows") -> Platform.WINDOWS(uname)
      osName.startsWith("linux") -> Platform.LINUX(uname)
      osName.startsWith("mac") -> Platform.MACOS(uname)
      osName.startsWith("darwin") -> Platform.MACOS(uname)
      else -> error("unknown osName: $osName")
    }
  }


  override suspend fun executeCommandAndCaptureOutput(command: List<String>, options: ExecuteCommandOptions): String =
    withContext(Dispatchers.IO) {
      val process = ProcessBuilder().apply {
        command(command.filter { it.isNotBlank() })
        directory(File(options.directory))
      }.start()
      val stdout = process.inputStream.bufferedReader().use { it.readText() }
      val stderr = process.errorStream.bufferedReader().use { it.readText() }
      val exitCode = runInterruptible { process.waitFor() }
      if (options.abortOnError) assert(exitCode == 0)
      val output = if (stderr.isBlank()) stdout else "$stdout $stderr"
      if (options.trim) output.trim() else output
    }

  override suspend fun pwd(options: ExecuteCommandOptions): String =
    File(".").absolutePath

  override suspend fun findExecutable(executable: String): String =
    when (platform()) {
      is Platform.WINDOWS -> executeCommandAndCaptureOutput(
        listOf("where", executable),
        ExecuteCommandOptions(".", true, false, true)
      )

      else -> executeCommandAndCaptureOutput(
        listOf("which", executable),
        ExecuteCommandOptions(".", true, false, true)
      )
    }
}
