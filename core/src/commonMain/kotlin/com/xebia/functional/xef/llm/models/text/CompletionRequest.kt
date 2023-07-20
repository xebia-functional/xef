package com.xebia.functional.xef.llm.models.text

import com.xebia.functional.xef.auto.PromptConfiguration

data class CompletionRequest(
  val model: String,
  val user: String,
  val prompt: String,
  val suffix: String? = null,
  val maxTokens: Int? = null,
  val temperature: Double? = null,
  val topP: Double? = null,
  val n: Int? = null,
  val stream: Boolean? = null,
  val logprobs: Int? = null,
  val echo: Boolean? = null,
  val stop: List<String>? = null,
  val presencePenalty: Double = 0.0,
  val frequencyPenalty: Double = 0.0,
  val bestOf: Int = 1,
  val logitBias: Map<String, Int> = emptyMap(),
  val streamToStandardOut: Boolean = false,
  val promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
)
