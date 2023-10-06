package com.xebia.functional.xef.llm

import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponse
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponseWithFunctions

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
