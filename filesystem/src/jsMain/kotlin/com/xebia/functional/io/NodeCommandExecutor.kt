package com.xebia.functional.io

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private external val child_process: dynamic
private external val os: dynamic

private open external class ExecOptions {
  var cwd: String
}

actual val CommandExecutor.Companion.DEFAULT: CommandExecutor
  get() = NodeCommandExecutor

object NodeCommandExecutor : CommandExecutor {
  override suspend fun executeCommandAndCaptureOutput(command: List<String>, options: ExecuteCommandOptions): String {
    val commandToExecute = command.joinToString(separator = " ") { arg ->
      if (arg.contains(" ")) "'$arg'" else arg
    }
    val redirect = if (options.redirectStderr) "2>&1 " else ""
    val execOptions = object : ExecOptions() {
      init {
        cwd = options.directory
      }
    }
    return suspendCoroutine { continuation ->
      child_process.exec("$commandToExecute $redirect", execOptions) { error, stdout, stderr ->
        if (error != null) {
          println(stderr)
          continuation.resumeWithException(error)
        } else {
          continuation.resume(if (options.trim) stdout.trim() else stdout)
        }
      }
      Unit
    }
  }

  override suspend fun pwd(options: ExecuteCommandOptions): String =
    //  https://nodejs.org/api/os.html
    when (os.platform()) {
      "win32" -> executeCommandAndCaptureOutput(listOf("echo", "%cd%"), options).trim()
      else -> executeCommandAndCaptureOutput(listOf("pwd"), options).trim()
    }

  override suspend fun findExecutable(executable: String): String = executable
}
