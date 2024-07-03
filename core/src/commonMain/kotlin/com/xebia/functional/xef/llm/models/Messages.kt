package com.xebia.functional.xef.llm.models

import com.xebia.functional.xef.openapi.CompletionUsage

data class MessagesWithUsage(val messages: List<String>, val usage: MessagesUsage?)

data class MessageWithUsage(val message: String, val usage: MessagesUsage?)

data class MessagesUsage(val completionTokens: Int, val promptTokens: Int, val totalTokens: Int) {
  companion object {
    operator fun invoke(usage: CompletionUsage) =
      MessagesUsage(
        completionTokens = usage.completionTokens,
        promptTokens = usage.promptTokens,
        totalTokens = usage.totalTokens
      )
  }
}
