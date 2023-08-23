package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.conversation.serialization.JacksonSerialization
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role

class JvmPromptBuilder : PlatformPromptBuilder() {
  override val items: MutableList<Message> = mutableListOf()

  override fun preprocess(elements: List<Message>): List<Message> = elements

  override fun build(): Prompt = Prompt(preprocess(items), null)

  fun <A> addSystemContent(content: A): PlatformPromptBuilder = apply {
    items.add(JacksonSerialization.objectMapper.writeValueAsString(content).message(Role.SYSTEM))
  }

  fun <A> addAssistantContent(content: A): PlatformPromptBuilder = apply {
    items.add(JacksonSerialization.objectMapper.writeValueAsString(content).message(Role.ASSISTANT))
  }

  fun <A> addUserContent(content: A): PlatformPromptBuilder = apply {
    items.add(JacksonSerialization.objectMapper.writeValueAsString(content).message(Role.USER))
  }
}
