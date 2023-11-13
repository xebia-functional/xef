package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.CreateChatCompletionResponse
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt

fun CreateChatCompletionResponse.addMetrics(
  conversation: Conversation
): CreateChatCompletionResponse {
  conversation.metric.parameter("model", model)
  conversation.metric.parameter(
    "tokens",
    "${usage?.promptTokens} (prompt) + ${usage?.completionTokens} (completion) = ${usage?.totalTokens}"
  )
  return this
}

fun Prompt.addMetrics(conversation: Conversation) {
  conversation.metric.parameter(
    "number-of-messages",
    "${messages.size} (${messages.map { it.completionRole().value.firstOrNull() ?: "" }.joinToString("-")})"
  )
  conversation.metric.parameter("conversation-id", conversation.conversationId?.value ?: "none")
  conversation.metric.parameter("functions", function?.let { "yes" } ?: "no")
  conversation.metric.parameter("temperature", "${configuration.temperature}")
}
