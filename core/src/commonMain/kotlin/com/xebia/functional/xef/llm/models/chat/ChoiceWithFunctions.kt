package com.xebia.functional.xef.llm.models.chat

data class ChoiceWithFunctions(
  val message: MessageWithFunctionCall?,
  val finishReason: String?,
  val index: Int?
)
