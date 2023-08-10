package com.xebia.functional.xef.io

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import platform.posix._pclose
import platform.posix._popen
import platform.posix.chdir
import platform.posix.fgets

actual val CommandExecutor.Companion.DEFAULT: CommandExecutor
  get() = WindowsCommandExecutor

object WindowsCommandExecutor : CommandExecutor {
  // TODO Remove RunBlocking
  private val platform: Platform by lazy {
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
    // if (uname.isNotBlank()) println("uname: $uname")
    when {
      uname.startsWith("MSYS") -> Platform.LINUX
      uname.startsWith("MINGW") -> Platform.LINUX
      uname.startsWith("CYGWIN") -> Platform.LINUX
      else -> Platform.WINDOWS
    }//.also { println("platform is $it") }
  }

  /**
   * https://stackoverflow.com/questions/57123836/kotlin-native-execute-command-and-get-the-output
   */
  @OptIn(ExperimentalForeignApi::class)
  override suspend fun executeCommandAndCaptureOutput(command: List<String>, options: ExecuteCommandOptions): String {
    chdir(options.directory)
    val commandToExecute = command.joinToString(separator = " ") { arg ->
      if (arg.contains(" ") || arg.contains("%")) "\"$arg\"" else arg
    }
    println("executing: $commandToExecute")
    val redirect = if (options.redirectStderr) " 2>&1 " else ""
    val fp = _popen("$commandToExecute $redirect", "r") ?: error("Failed to run command: $command")

    val stdout = buildString {
      val buffer = ByteArray(4096)
      while (true) {
        val input = fgets(buffer.refTo(0), buffer.size, fp) ?: break
        append(input.toKString())
      }
    }

    val status = _pclose(fp)
    if (status != 0 && options.abortOnError) {
      println(stdout)
      println("failed to run: $commandToExecute")
      throw Exception("Command `$command` failed with status $status${if (options.redirectStderr) ": $stdout" else ""}")
    }

    return if (options.trim) stdout.trim() else stdout
  }


  override suspend fun pwd(options: ExecuteCommandOptions): String = when (platform) {
    Platform.WINDOWS -> executeCommandAndCaptureOutput(listOf("echo", "%cd%"), options).trim('"', ' ')
    else -> executeCommandAndCaptureOutput(listOf("pwd"), options).trim()
  }

  override suspend fun findExecutable(executable: String): String =
    executable
}
