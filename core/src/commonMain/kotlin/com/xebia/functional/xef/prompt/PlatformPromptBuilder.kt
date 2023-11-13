package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage

class PlatformPromptBuilder : PromptBuilder {

  override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> = elements

  override fun build(): Prompt = Prompt(preprocess(items))

  companion object {
    fun create(): PlatformPromptBuilder = PlatformPromptBuilder()
  }
}
