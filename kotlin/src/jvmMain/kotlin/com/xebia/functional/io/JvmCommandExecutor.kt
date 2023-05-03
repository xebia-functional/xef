package com.xebia.functional.io

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

val CommandExecutor.Companion.SYSTEM: CommandExecutor
  get() = JvmCommandExecutor

object JvmCommandExecutor : CommandExecutor {

  // TODO Remove RunBlocking
  private val platform: Platform by lazy {
    val osName = System.getProperty("os.name").lowercase()

    when {
      osName.startsWith("windows") -> {
        val uname = runBlocking {
          try {
            executeCommandAndCaptureOutput(
              listOf("where", "uname"),
              ExecuteCommandOptions(
                directory = ".",
                abortOnError = true,
                redirectStderr = false,
                trim = true,
              ),
            )
            executeCommandAndCaptureOutput(
              listOf("uname", "-a"),
              ExecuteCommandOptions(
                directory = ".",
                abortOnError = true,
                redirectStderr = true,
                trim = true,
              ),
            )
          } catch (e: Exception) {
            ""
          }
        }
//        if (uname.isNotBlank()) println("uname: $uname")
        when {
          uname.startsWith("MSYS") -> Platform.LINUX
          uname.startsWith("MINGW") -> Platform.LINUX
          uname.startsWith("CYGWIN") -> Platform.LINUX
          else -> Platform.WINDOWS
        } // .also { println("platform is $it") }
      }

      osName.startsWith("linux") -> Platform.LINUX
      osName.startsWith("mac") -> Platform.MACOS
      osName.startsWith("darwin") -> Platform.MACOS
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
    when (platform) {
      Platform.WINDOWS -> executeCommandAndCaptureOutput(
        listOf("where", executable),
        ExecuteCommandOptions(".", true, false, true)
      )

      else -> executeCommandAndCaptureOutput(
        listOf("which", executable),
        ExecuteCommandOptions(".", true, false, true)
      )
    }
}
