package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message

class NativePromptBuilder : PlatformPromptBuilder() {
  override val items: MutableList<Message> = mutableListOf()

  override fun preprocess(elements: List<Message>): List<Message> = elements

  override fun build(): Prompt = Prompt(preprocess(items), null)
}
