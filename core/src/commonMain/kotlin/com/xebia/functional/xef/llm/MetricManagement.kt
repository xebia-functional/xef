package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponse
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponseWithFunctions
import com.xebia.functional.xef.prompt.Prompt

fun ChatCompletionResponseWithFunctions.addMetrics(
  conversation: Conversation
): ChatCompletionResponseWithFunctions {
  conversation.metric.parameter("model", `object`)
  conversation.metric.parameter("promptTokens", usage.promptTokens.toString())
  conversation.metric.parameter("completionTokens", usage.completionTokens.toString())
  conversation.metric.parameter("totalTokens", usage.totalTokens.toString())
  return this
}

fun ChatCompletionResponse.addMetrics(conversation: Conversation): ChatCompletionResponse {
  conversation.metric.parameter("model", `object`)
  conversation.metric.parameter(
    "tokens",
    "${usage.promptTokens} (prompt) + ${usage.completionTokens} (completion) = ${usage.totalTokens}"
  )
  return this
}

fun Prompt.addMetrics(conversation: Conversation) {
  conversation.metric.parameter(
    "number-of-messages",
    "${messages.size} (${messages.map { it.role.toString().firstOrNull() ?: "" }.joinToString("-")})"
  )
  conversation.metric.parameter("conversation-id", conversation.conversationId?.value ?: "none")
  conversation.metric.parameter("functions", function?.let { "yes" } ?: "no")
  conversation.metric.parameter("temperature", "${configuration.temperature}")
}
