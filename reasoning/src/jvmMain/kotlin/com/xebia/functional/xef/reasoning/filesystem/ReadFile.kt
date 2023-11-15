package com.xebia.functional.xef.reasoning.filesystem

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.io.DEFAULT
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.reasoning.tools.Tool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import okio.FileSystem
import okio.Path.Companion.toPath

@Serializable data class ExtractedFile(val absolutePath: String)

class ReadFile(private val model: ChatWithFunctions, private val scope: Conversation) : Tool {

  private val logger = KotlinLogging.logger {}

  override val name: String = "Read File"

  override val description: String = "Reads the content of a file as String"

  override suspend fun invoke(input: String): String {
    val extractedFile: ExtractedFile =
      model.prompt(
        prompt =
          Prompt(
            """|
        |Please provide the absolute path to the file you want to read
        |given this input:
        |
        |$input
      """
              .trimMargin()
          ),
        scope = scope,
        serializer = ExtractedFile.serializer()
      )

    logger.info { "Reading file ${extractedFile.absolutePath}" }

    return FileSystem.DEFAULT.read(extractedFile.absolutePath.toPath()) { readUtf8() }
  }
}
