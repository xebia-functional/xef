package com.xebia.functional.xef

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.openapi.Chat
import com.xebia.functional.xef.openapi.CreateChatCompletionRequest
import com.xebia.functional.xef.openapi.OpenAI

data class AIConfig(
  val tools: List<Tool<*>> = emptyList(),
  val model: CreateChatCompletionRequest.Model = CreateChatCompletionRequest.Model.Gpt4o,
  val config: Config = Config(),
  val openAI: OpenAI = OpenAI(config, logRequests = false),
  val api: Chat = openAI.chat,
  val conversation: Conversation = Conversation()
)
