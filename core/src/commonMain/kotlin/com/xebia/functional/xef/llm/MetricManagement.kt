package com.xebia.functional.xef.llm

import com.xebia.functional.openai.models.CreateChatCompletionResponse
import com.xebia.functional.openai.models.RunObject
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.metrics.Metric
import com.xebia.functional.xef.prompt.Prompt

suspend fun CreateChatCompletionResponse.addMetrics(
  conversation: Conversation
): CreateChatCompletionResponse {
  conversation.metric.parameter("openai.chat_completion.model", model)
  usage?.let {
    conversation.metric.parameter("openai.chat_completion.prompt.token.count", "${it.promptTokens}")
    conversation.metric.parameter(
      "openai.chat_completion.completion.token.count",
      "${it.completionTokens}"
    )
    conversation.metric.parameter("openai.chat_completion.token.count", "${it.totalTokens}")
  }
  return this
}

suspend fun <T> Prompt<T>.addMetrics(conversation: Conversation) {
  conversation.metric.parameter("openai.chat_completion.prompt.message.count", "${messages.size}")

  conversation.metric.parameter(
    "openai.chat_completion.prompt.messages_roles",
    messages.map { it.completionRole().value }
  )
  conversation.metric.parameter(
    "openai.chat_completion.prompt.last-message",
    messages.lastOrNull()?.contentAsString() ?: "empty"
  )
  conversation.metric.parameter(
    "openai.chat_completion.conversation_id",
    conversation.conversationId?.value ?: "none"
  )
  conversation.metric.parameter(
    "openai.chat_completion.prompt.temperature",
    "${configuration.temperature}"
  )
  if (functions.isNotEmpty())
    conversation.metric.parameter("openai.chat_completion.functions", functions.map { it.name })
}

suspend fun RunObject.addMetrics(metric: Metric): RunObject {
  metric.assistantCreateRun(this)
  return this
}
