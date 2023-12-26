package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.CreateChatCompletionResponse
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt

suspend fun CreateChatCompletionResponse.addMetrics(
  conversation: Conversation
): CreateChatCompletionResponse {
  conversation.metric.parameter("model", model)
  conversation.metric.parameter(
    "tokens",
    "${usage?.promptTokens} (prompt) + ${usage?.completionTokens} (completion) = ${usage?.totalTokens}"
  )
  return this
}

suspend fun <T> Prompt<T>.addMetrics(conversation: Conversation) {
  conversation.metric.parameter(
    "number-of-messages",
    "${messages.size} (${messages.map { it.completionRole().value.firstOrNull() ?: "" }.joinToString("-")})"
  )
  conversation.metric.parameter("conversation-id", conversation.conversationId?.value ?: "none")
  conversation.metric.parameter(
    "functions",
    if (functions.isEmpty()) "no" else functions.joinToString(",") { it.name }
  )
  conversation.metric.parameter("temperature", "${configuration.temperature}")
}
