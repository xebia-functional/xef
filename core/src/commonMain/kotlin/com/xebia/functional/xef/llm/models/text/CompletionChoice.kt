package com.xebia.functional.xef.llm.models.text

data class CompletionChoice(
  val text: String,
  val index: Int,
  val logprobs: Int? = null,
  val finishReason: String?
)
