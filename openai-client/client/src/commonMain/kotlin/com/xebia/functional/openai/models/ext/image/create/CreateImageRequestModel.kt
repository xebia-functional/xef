package com.xebia.functional.openai.models.ext.image.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateImageRequestModel(val value: String) {
  @SerialName(value = "dall-e-2") `dall_e_2`("dall-e-2"),
  @SerialName(value = "dall-e-3") `dall_e_3`("dall-e-3")
}
