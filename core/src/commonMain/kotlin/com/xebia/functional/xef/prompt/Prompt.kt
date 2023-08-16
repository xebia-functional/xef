package com.xebia.functional.xef.prompt

import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.prompt.templates.user
import kotlinx.serialization.Serializable

/** A Prompt is a serializable list of messages. The messages may involve different roles. */
@Serializable
data class Prompt(val messages: List<Message>) {

  constructor(value: String) : this(listOf(user(value)))
}
