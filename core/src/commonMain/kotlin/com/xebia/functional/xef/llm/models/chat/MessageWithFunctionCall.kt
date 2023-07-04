package com.xebia.functional.xef.llm.models.chat

import com.xebia.functional.xef.llm.models.functions.FunctionCall

data class MessageWithFunctionCall(
  val role: String,
  val content: String? = null,
  val functionCall: FunctionCall?,
  val name: String? = Role.ASSISTANT.name
)
