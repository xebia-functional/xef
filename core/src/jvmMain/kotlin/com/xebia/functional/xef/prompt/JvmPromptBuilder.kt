package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.models.ChatCompletionRequestMessage
import com.xebia.functional.openai.models.ChatCompletionRole.*
import com.xebia.functional.xef.conversation.serialization.JacksonSerialization

class JvmPromptBuilder : PlatformPromptBuilder() {
  override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  override fun preprocess(elements: List<ChatCompletionRequestMessage>): List<ChatCompletionRequestMessage> = elements

  override fun build(): Prompt = Prompt(preprocess(items), null)

  fun <A> addSystemContent(content: A): PlatformPromptBuilder = apply {
    items.add(JacksonSerialization.objectMapper.writeValueAsString(content).message(system))
  }

  fun <A> addAssistantContent(content: A): PlatformPromptBuilder = apply {
    items.add(JacksonSerialization.objectMapper.writeValueAsString(content).message(assistant))
  }

  fun <A> addUserContent(content: A): PlatformPromptBuilder = apply {
    items.add(JacksonSerialization.objectMapper.writeValueAsString(content).message(user))
  }
}
