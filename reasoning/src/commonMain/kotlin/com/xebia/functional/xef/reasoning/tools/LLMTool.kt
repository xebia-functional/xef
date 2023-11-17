package com.xebia.functional.xef.reasoning.tools

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import com.xebia.functional.xef.prompt.templates.system
import com.xebia.functional.xef.prompt.templates.user
import com.xebia.functional.xef.reasoning.internals.callModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

abstract class LLMTool(
  override val name: String,
  override val description: String,
  private val chatApi: ChatApi,
  private val model: OpenAIModel<CreateChatCompletionRequestModel>,
  private val scope: Conversation,
  private val instructions: List<String> = emptyList()
) : Tool {
  private val logger = KotlinLogging.logger {}

  override suspend operator fun invoke(input: String): String {
    logger.info { "🔧 $name[$input]" }

    return callModel(
      chatApi,
      scope,
      prompt =
        Prompt(model) {
          +system("You are an expert in executing the tool:")
          +system("Tool: $name")
          +system("Description: $description")
          instructions.forEach { +system(it) }
          +user("input: $input")
          +assistant("output:")
        }
    )
  }

  companion object {
    @JvmStatic
    @JvmOverloads
    fun create(
      name: String,
      description: String,
      chatApi: ChatApi,
      model: OpenAIModel<CreateChatCompletionRequestModel>,
      scope: Conversation,
      instructions: List<String> = emptyList()
    ): LLMTool = object : LLMTool(name, description, chatApi, model, scope, instructions) {}
  }
}
