package com.xebia.functional.xef.prompt

import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import com.xebia.functional.openai.models.ext.chat.create.CreateChatCompletionRequestModel

class PlatformPromptBuilder(private val model: CreateChatCompletionRequestModel) : PromptBuilder {

  override val items: MutableList<ChatCompletionRequestMessage> = mutableListOf()

  override fun preprocess(
    elements: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> = elements

  override fun build(): Prompt = Prompt(model, preprocess(items))

  companion object {
    fun create(model: CreateChatCompletionRequestModel): PlatformPromptBuilder = PlatformPromptBuilder(model)
  }
}
