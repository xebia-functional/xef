package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.CreateChatCompletionResponse
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt

suspend fun CreateChatCompletionResponse.addMetrics(
  conversation: Conversation
): CreateChatCompletionResponse {
  conversation.metric.parameter("openai.chat_completion.model", model)
  usage?.let {
    conversation.metric.parameter("openai.chat_completion.prompt.token.count", "${it.promptTokens}")
    conversation.metric.parameter("openai.chat_completion.completion.token.count", "${it.completionTokens}")
    conversation.metric.parameter("openai.chat_completion.token.count", "${it.totalTokens}")
  }
  return this
}

suspend fun <T> Prompt<T>.addMetrics(conversation: Conversation) {
  conversation.metric.parameter(
    "openai.chat_completion.prompt.message.count",
    "${messages.size} (${messages.map { it.completionRole().value.firstOrNull() ?: "" }.joinToString("-")})"
  )
  conversation.metric.parameter("openai.chat_completion.conversation.id", conversation.conversationId?.value ?: "none")
  conversation.metric.parameter(
    "functions",
    if (functions.isEmpty()) "no" else functions.joinToString(",") { it.name }
  )
  conversation.metric.parameter("openai.chat_completion.prompt.temperature", "${configuration.temperature}")
}
