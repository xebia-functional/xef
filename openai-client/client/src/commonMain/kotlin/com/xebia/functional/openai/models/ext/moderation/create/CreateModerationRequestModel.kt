package com.xebia.functional.openai.models.ext.moderation.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateModerationRequestModel(val value: String) {
  @SerialName(value = "text-moderation-latest") `text_moderation_latest`("text-moderation-latest"),
  @SerialName(value = "text-moderation-stable") `text_moderation_stable`("text-moderation-stable")
}
