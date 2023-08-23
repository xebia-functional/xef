package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message

actual abstract class PlatformPromptBuilder : PromptBuilder {
  actual override val items: MutableList<Message> = mutableListOf()

  actual override fun preprocess(elements: List<Message>): List<Message> = elements

  actual override fun build(): Prompt = Prompt(preprocess(items))

  actual companion object {
    actual fun create(): PlatformPromptBuilder = NativePromptBuilder()
  }
}
