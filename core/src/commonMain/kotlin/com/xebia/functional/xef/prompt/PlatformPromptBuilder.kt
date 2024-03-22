package com.xebia.functional.xef.prompt

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.models.FunctionObject
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration

class PlatformPromptBuilder<T>(
  private val model: OpenAIModel<T>,
  private val functions: List<FunctionObject>,
  private val configuration: PromptConfiguration
) : PromptBuilder<T> {

  override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> = elements

  override fun build(): Prompt<T> = Prompt(model, preprocess(items), functions, configuration)

  companion object {
    fun <T> create(
      model: OpenAIModel<T>,
      functions: List<FunctionObject>,
      configuration: PromptConfiguration
    ): PlatformPromptBuilder<T> = PlatformPromptBuilder(model, functions, configuration)
  }
}
