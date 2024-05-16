package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.generated.model.ChatCompletionRequestMessage
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.openai.generated.model.FunctionObject
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration

class PlatformPromptBuilder(
  private val model: CreateChatCompletionRequestModel,
  private val functions: List<FunctionObject>,
  private val toolCallStrategy: ToolCallStrategy,
  private val configuration: PromptConfiguration
) : PromptBuilder {

  override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> = elements

  override fun build(): Prompt =
    Prompt(model, preprocess(items), functions, toolCallStrategy, configuration)

  companion object {
    fun create(
      model: CreateChatCompletionRequestModel,
      functions: List<FunctionObject>,
      toolCallStrategy: ToolCallStrategy,
      configuration: PromptConfiguration,
    ): PlatformPromptBuilder =
      PlatformPromptBuilder(model, functions, toolCallStrategy, configuration)
  }
}
