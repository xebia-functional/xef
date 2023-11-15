package com.xebia.functional.xef.llm.models.functions

import arrow.core.Nel
import com.xebia.functional.xef.llm.models.chat.Message

data class FunChatCompletionRequest(
  val messages: List<Message>,
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
  val streamToStandardOut: Boolean = false,
  val functions: Nel<CFunction>,
  val functionCall: Map<String, String>? = null,
)
