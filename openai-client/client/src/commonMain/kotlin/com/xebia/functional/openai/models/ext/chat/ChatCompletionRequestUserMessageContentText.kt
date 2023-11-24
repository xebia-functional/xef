package com.xebia.functional.openai.models.ext.chat

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestUserMessageContentText(
  @SerialName(value = "text") @Required val text: String,
  @SerialName(value = "type") @Required val type: Type
) : ChatCompletionRequestUserMessageContent() {

  constructor(text: String) : this(text, Type.text)

  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "text") text("text")
  }
}
