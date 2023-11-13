package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage

actual abstract class PlatformPromptBuilder : PromptBuilder {
  actual override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  actual override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> = elements

  actual override fun build(): Prompt = Prompt(preprocess(items))

  actual companion object {
    actual fun create(): PlatformPromptBuilder = JvmPromptBuilder()
  }
}
