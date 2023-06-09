package com.xebia.functional.xef.llm.models.chat

import com.xebia.functional.xef.llm.models.functions.FunctionCall

data class ChatDelta(
  /** The role of the author of this message. */
  val role: Role? = null,

  /** The contents of the message. */
  val content: String? = null,

  /** The name and arguments of a function that should be called, as generated by the model. */
  val functionCall: FunctionCall? = null
)
