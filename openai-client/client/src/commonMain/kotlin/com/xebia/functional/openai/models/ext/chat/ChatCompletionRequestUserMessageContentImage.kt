package com.xebia.functional.openai.models.ext.chat

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestUserMessageContentImage(
  @SerialName(value = "type") @Required val type: Type,
  @SerialName(value = "image_url")
  @Required
  val imageUrl: ChatCompletionRequestUserMessageContentImageUrl
) : ChatCompletionRequestUserMessageContent() {
  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "image_url") imageUrl("image_url")
  }
}
