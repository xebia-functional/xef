package com.xebia.functional.xef.llm.models.chat

import com.xebia.functional.xef.llm.models.functions.FunctionCall

data class MessageWithFunctionCall(
  val role: String,
  val content: String? = null,
  val functionCall: FunctionCall?,
  val name: String? = Role.ASSISTANT.name
) {
  fun toMessage(): Message {
    val role = role.uppercase().let { Role.valueOf(it) } // TODO valueOf is unsafe
    return Message(
      role = role,
      content = content ?: functionCall?.arguments ?: "",
      name = role.name
    )
  }
}
