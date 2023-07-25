package com.xebia.functional.xef.llm.models.chat

import com.xebia.functional.xef.llm.models.functions.CFunction

data class ChatCompletionRequestWithFunctions(
  val model: String,
  val messages: List<Message>,
  val functions: List<CFunction> = emptyList(),
  val temperature: Double = 0.0,
  val topP: Double = 1.0,
  val n: Int = 1,
  val stream: Boolean = false,
  val stop: List<String>? = null,
  val maxTokens: Int? = null,
  val presencePenalty: Double = 0.0,
  val frequencyPenalty: Double = 0.0,
  val logitBias: Map<String, Int> = emptyMap(),
  val user: String?,
  val functionCall: Map<String, String>
)
