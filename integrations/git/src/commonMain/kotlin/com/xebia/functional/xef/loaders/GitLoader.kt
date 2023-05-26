package com.xebia.functional.xef.loaders

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.io.CommandExecutor
import com.xebia.functional.xef.io.DEFAULT
import com.xebia.functional.xef.io.ExecuteCommandOptions
import com.xebia.functional.xef.io.isDirectory
import com.xebia.functional.xef.textsplitters.TextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import io.github.oshai.KotlinLogging
import okio.FileSystem
import okio.Path

suspend fun git(
  url: String,
  command: List<String> = GitLoader.defaultCommand(url),
  textSplitter: TextSplitter = TokenTextSplitter(
    modelType = ModelType.GPT_3_5_TURBO,
    chunkSize = 1000,
    chunkOverlap = 100
  ),
  fileSystem: FileSystem = FileSystem.DEFAULT,
  exec: CommandExecutor = CommandExecutor.DEFAULT,
  fileFilter: (Path) -> Boolean = { true }
) =
  GitLoader(url, command, fileSystem, exec, fileFilter).loadAndSplit(textSplitter)

class GitLoader(private val url: String,
                private val command: List<String> = defaultCommand(url),
                private val fileSystem: FileSystem = FileSystem.DEFAULT,
                private val exec: CommandExecutor = CommandExecutor.DEFAULT,
                private val fileFilter: (Path) -> Boolean = { true }) : BaseLoader {

  private val logger = KotlinLogging.logger {}

  override suspend fun load(): List<String> {
    val workingDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY
    val localLocation = workingDir.resolve(url.substringAfterLast("/"))
    fileSystem.deleteRecursively(localLocation, false)
    val cloneOutput = exec.executeCommandAndCaptureOutput(
      command,
      ExecuteCommandOptions(
        directory = workingDir.toString(),
        abortOnError = true,
        redirectStderr = false,
        trim = true,
      ),
    )
    logger.debug { "🐙: $cloneOutput" }
    val repositoryFiles =
      fileSystem.listRecursively(workingDir.resolve(url.substringAfterLast("/")))
    val fileMap = repositoryFiles
      .filter { path -> fileFilter(path) && !fileSystem.isDirectory(path) }
      .toList().associate {
        it.relativeTo(localLocation).toString() to TextLoader(it, fileSystem).load() }
    // the text file emoji is a
    logger.debug { "📄: indexing ${fileMap.entries.size} files" }
    return fileMap.map {
      it.key + ":\n" + it.value.joinToString("\n")
    }
  }

  companion object {
    fun defaultCommand(url: String): List<String> =
      listOf("git", "clone", "--depth=1", url)
  }

}

