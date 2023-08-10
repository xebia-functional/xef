package com.xebia.functional.xef.reasoning.tools

import com.xebia.functional.xef.auto.CoreAIScope
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

abstract class LLMTool(
  override val name: String,
  override val description: String,
  private val model: Chat,
  private val scope: CoreAIScope,
  private val instructions: List<String> = emptyList()
) : Tool {
  private val logger = KotlinLogging.logger {}

  override suspend operator fun invoke(input: String): String {
    logger.info { "🔧 $name[$input]" }

    return callModel(
      model,
      scope,
      prompt =
        listOf(
          Message.systemMessage { "You are an expert in executing tool:" },
          Message.systemMessage { "Tool: $name" },
          Message.systemMessage { "Description: $description" },
        ) +
          instructions.map { Message.systemMessage { it } } +
          listOf(
            Message.userMessage { "input: $input" },
            Message.assistantMessage { "output:" },
          )
    )
  }

  companion object {
    @JvmStatic
    @JvmOverloads
    fun create(
      name: String,
      description: String,
      model: Chat,
      scope: CoreAIScope,
      instructions: List<String> = emptyList()
    ): LLMTool = object : LLMTool(name, description, model, scope, instructions) {}
  }
}
