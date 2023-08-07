package com.xebia.functional.xef.prompt.expressions

import com.xebia.functional.xef.llm.models.chat.Message

data class ExpressionResult(
  val messages: List<Message>,
  val result: String,
  val values: ReplacedValues,
)
