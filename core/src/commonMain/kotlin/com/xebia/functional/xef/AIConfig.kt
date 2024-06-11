package com.xebia.functional.xef

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.api.OpenAI
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation

data class AIConfig(
  val tools: List<Tool<*>> = emptyList(),
  val model: CreateChatCompletionRequestModel = CreateChatCompletionRequestModel.gpt_4o,
  val config: Config = Config(),
  val openAI: OpenAI = OpenAI(config),
  val api: Chat = openAI.chat,
  val conversation: Conversation = Conversation(),
)
