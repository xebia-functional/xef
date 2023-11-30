package com.xebia.functional.xef.prompt

import ai.xef.openai.OpenAIModel
import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage

class PlatformPromptBuilder<T>(private val model: OpenAIModel<T>) : PromptBuilder<T> {

  override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> = elements

  override fun build(): Prompt<T> = Prompt(model, preprocess(items))

  companion object {
    fun <T> create(model: OpenAIModel<T>): PlatformPromptBuilder<T> = PlatformPromptBuilder(model)
  }
}
