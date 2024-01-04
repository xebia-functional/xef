package com.xebia.functional.openai.models.ext.chat

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestUserMessageContentImage(
  @SerialName(value = "image_url")
  @Required
  val imageUrl: ChatCompletionRequestUserMessageContentImageUrl,
  @SerialName(value = "type") @Required val type: Type
) : ChatCompletionRequestUserMessageContent() {

  constructor(
    imageUrl: ChatCompletionRequestUserMessageContentImageUrl
  ) : this(imageUrl, Type.imageUrl)

  @Serializable
  enum class Type(val value: String) {
    @SerialName(value = "image_url") imageUrl("image_url")
  }
}
