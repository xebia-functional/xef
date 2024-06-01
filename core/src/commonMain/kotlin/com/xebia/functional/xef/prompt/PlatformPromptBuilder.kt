package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.ChatCompletionRequestMessage
import com.xebia.functional.xef.llm.FunctionObject
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration

class PlatformPromptBuilder(
  private val functions: List<FunctionObject>,
  private val configuration: PromptConfiguration
) : PromptBuilder {

  override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> = elements

  override fun build(): Prompt = Prompt(preprocess(items), functions, configuration)

  companion object {
    fun create(
      functions: List<FunctionObject>,
      configuration: PromptConfiguration
    ): PlatformPromptBuilder = PlatformPromptBuilder(functions, configuration)
  }
}
