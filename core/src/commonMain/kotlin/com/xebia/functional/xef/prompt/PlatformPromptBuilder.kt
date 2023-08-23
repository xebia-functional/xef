package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message

expect abstract class PlatformPromptBuilder() : PromptBuilder {

  override val items: MutableList<Message>

  override fun preprocess(elements: List<Message>): List<Message>

  override fun build(): Prompt

  companion object {
    fun create(): PlatformPromptBuilder
  }
}
