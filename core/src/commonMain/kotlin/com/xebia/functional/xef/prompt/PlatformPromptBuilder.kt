package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import io.github.nomisrev.openapi.ChatCompletionRequestMessage
import io.github.nomisrev.openapi.CreateChatCompletionRequest
import io.github.nomisrev.openapi.FunctionObject

class PlatformPromptBuilder(
  private val model: CreateChatCompletionRequest.Model,
  private val functions: List<FunctionObject>,
  private val configuration: PromptConfiguration
) : PromptBuilder {

  override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> = elements

  override fun build(): Prompt = Prompt(model, preprocess(items), functions, configuration)

  companion object {
    fun create(
      model: CreateChatCompletionRequest.Model,
      functions: List<FunctionObject>,
      configuration: PromptConfiguration
    ): PlatformPromptBuilder = PlatformPromptBuilder(model, functions, configuration)
  }
}
