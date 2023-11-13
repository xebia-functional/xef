package com.xebia.functional.openai.models.ext.image.edit.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateImageEditRequestModel(val value: String) {
  @SerialName(value = "dall-e-2") dalle2("dall-e-2")
}
