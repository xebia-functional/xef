package com.xebia.functional.xef.loaders

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.io.*
import com.xebia.functional.xef.textsplitters.TextSplitter
import com.xebia.functional.xef.textsplitters.TokenTextSplitter
import io.github.oshai.KotlinLogging
import okio.FileSystem
import okio.Path

data class FileContent(val path: Path, val content: String) {
  fun toDocument(): String = """|
    |${path.name}
    |
    |$content
  """.trimMargin()
}

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
  fileLoader: suspend (FileContent) -> List<String> = { listOf(it.content) },
  fileFilter: (Path) -> Boolean = { true }
) =
  GitLoader(url, command, fileSystem, exec, fileLoader, fileFilter).loadAndSplit(textSplitter)

class GitLoader(private val url: String,
                private val command: List<String> = defaultCommand(url),
                private val fileSystem: FileSystem = FileSystem.DEFAULT,
                private val exec: CommandExecutor = CommandExecutor.DEFAULT,
                private val fileLoader: suspend (FileContent) -> List<String> = { listOf(it.content) },
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
    return repositoryFiles
      .filter { path -> fileFilter(path) && !fileSystem.isDirectory(path) }
      .toList().flatMap { fileLoader(FileContent(it, fileSystem.read(it) { readUtf8() })) }
  }

  companion object {
    fun defaultCommand(url: String): List<String> =
      listOf("git", "clone", "--depth=1", url)
  }

}

