package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponse
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponseWithFunctions
import com.xebia.functional.xef.prompt.Prompt

fun ChatCompletionResponseWithFunctions.addMetrics(
  conversation: Conversation
): ChatCompletionResponseWithFunctions {
  conversation.metric.log(conversation, "Model: ${`object`}")
  conversation.metric.log(
    conversation,
    "Tokens: ${usage.promptTokens} (prompt) + ${usage.completionTokens} (completion) = ${usage.totalTokens}"
  )
  return this
}

fun ChatCompletionResponse.addMetrics(conversation: Conversation): ChatCompletionResponse {
  conversation.metric.log(conversation, "Model: ${`object`}")
  conversation.metric.log(
    conversation,
    "Tokens: ${usage.promptTokens} (prompt) + ${usage.completionTokens} (completion) = ${usage.totalTokens}"
  )
  return this
}

fun Prompt.addMetrics(conversation: Conversation) {
  conversation.metric.log(
    conversation,
    "Number of messages: ${messages.size} (${messages.map { it.role.toString().firstOrNull() ?: "" }.joinToString("-")})"
  )
  conversation.metric.log(conversation, "Functions: ${function?.let { "yes" } ?: "no"}")
  conversation.metric.log(conversation, "Temperature: ${configuration.temperature}")
}
