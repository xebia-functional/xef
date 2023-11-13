package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage

expect abstract class PlatformPromptBuilder() : PromptBuilder {

  override val items: MutableList<ChatCompletionRequestMessage>

  override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage>

  override fun build(): Prompt

  companion object {
    fun create(): PlatformPromptBuilder
  }
}
