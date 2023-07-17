package com.xebia.functional.xef.reasoning.code.documentation

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.prompt.experts.ExpertSystem
import com.xebia.functional.xef.reasoning.tools.Tool
import com.xebia.functional.xef.reasoning.tools.ToolMetadata
import io.github.oshai.kotlinlogging.KotlinLogging

class CodeDocumentationGeneration(
  private val model: Chat,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool<CodeDocumentation> {

  private val logger = KotlinLogging.logger {}

  override val functions:
    Map<ToolMetadata, suspend (input: String) -> Tool.Out<CodeDocumentation>> =
    mapOf(
      ToolMetadata(
        name = "generateCodeDocumentation",
        description = "Generate code documentation"
      ) to ::generateCodeDocumentation
    )

  suspend fun generateCodeDocumentation(content: String): CodeDocumentation {
    logger.info { "🔍 Generating code documentation" }
    val outline = extractOutlineFromContent(content)
    val details = extractDetailsFromContent(content)
    val examples = extractExamplesFromContent(content)

    return CodeDocumentation(
      title = "Code Documentation",
      outline = outline,
      details = details,
      examples = examples
    )
  }

  private suspend fun extractOutlineFromContent(content: String): String {
    logger.info { "Extracting outline from content" }
    return model.promptMessage(
      ExpertSystem(
          system =
            "You are an expert in code documentation and can extract the outline from the provided content",
          query = "Extract the outline from the following content:\n\n```\n$content\n```",
          instructions =
            listOf("Your `RESPONSE` MUST be the outline of the code documentation") + instructions
        )
        .message,
      context = scope.context,
      conversationId = scope.conversationId
    )
  }

  private suspend fun extractDetailsFromContent(content: String): String {
    logger.info { "Extracting details from content" }
    return model.promptMessage(
      ExpertSystem(
          system =
            "You are an expert in code documentation and can extract the details from the provided content",
          query = "Extract the details from the following content:\n\n```\n$content\n```",
          instructions =
            listOf("Your `RESPONSE` MUST be the details of the code documentation") + instructions
        )
        .message,
      context = scope.context,
      conversationId = scope.conversationId
    )
  }

  private suspend fun extractExamplesFromContent(content: String): String {
    logger.info { "Extracting examples from content" }
    return model.promptMessage(
      ExpertSystem(
          system =
            "You are an expert in code documentation and can extract the examples from the provided content",
          query = "Extract the examples from the following content:\n\n```\n$content\n```",
          instructions =
            listOf(
              "Your `RESPONSE` MUST be a list of code examples inside triple backtick ``` code fences."
            ) + instructions
        )
        .message,
      context = scope.context,
      conversationId = scope.conversationId
    )
  }
}
