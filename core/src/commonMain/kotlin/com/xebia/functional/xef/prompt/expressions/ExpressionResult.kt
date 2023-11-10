package com.xebia.functional.xef.prompt.expressions

import com.xebia.functional.openai.models.ChatCompletionRequestMessage

data class ExpressionResult(
  val messages: List<ChatCompletionRequestMessage>,
  val result: String,
  val values: ReplacedValues,
)
