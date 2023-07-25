package com.xebia.functional.xef.reasoning.filesystem

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.io.CommandExecutor
import com.xebia.functional.xef.io.DEFAULT
import com.xebia.functional.xef.io.ExecuteCommandOptions
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.internals.callModel
import com.xebia.functional.xef.reasoning.tools.Tool
import okio.FileSystem

class CreatePythonScript
@JvmOverloads
constructor(
  private val model: Chat,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool {
  override val name: String = "Create Python Script"

  override val description: String =
    "Creates a Python script from the input, run it and capture its output"

  override suspend fun invoke(input: String): String {
    val script: String =
      callModel(
        model = model,
        scope = scope,
        ExpertSystem(
          system = "Create a Python script from the input",
          query = input,
          instructions = instructions,
        )
      )
    val requirements: String =
      callModel(
        model = model,
        scope = scope,
        ExpertSystem(
          system = "Create a requirements.txt for the input",
          query = script,
          instructions = instructions,
        )
      )
    val tempFile = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("data").resolve("script.py")
    FileSystem.DEFAULT.write(tempFile, mustCreate = true) { writeUtf8(script) }
    val tempRequirements =
      FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve("data").resolve("requirements.txt")
    FileSystem.DEFAULT.write(tempRequirements, mustCreate = true) { writeUtf8(requirements) }
    val output =
      CommandExecutor.DEFAULT.executeCommandAndCaptureOutput(
        command =
          listOf(
            "pip",
            "install",
            "-r",
            tempRequirements.toString(),
            "-t",
            tempRequirements.parent.toString()
          ),
        options =
          ExecuteCommandOptions(
            directory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString(),
            abortOnError = true,
            redirectStderr = true,
            trim = true
          )
      )
    val runOutput =
      CommandExecutor.DEFAULT.executeCommandAndCaptureOutput(
        command = listOf("python", tempFile.toString()),
        options =
          ExecuteCommandOptions(
            directory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString(),
            abortOnError = true,
            redirectStderr = true,
            trim = true
          )
      )
    return output + "\n" + runOutput
  }
}
