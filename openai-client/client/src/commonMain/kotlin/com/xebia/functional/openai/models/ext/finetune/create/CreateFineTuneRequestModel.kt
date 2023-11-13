package com.xebia.functional.openai.models.ext.finetune.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateFineTuneRequestModel(val value: String) {
  @SerialName(value = "ada") ada("ada"),
  @SerialName(value = "babbage") babbage("babbage"),
  @SerialName(value = "curie") curie("curie"),
  @SerialName(value = "davinci") davinci("davinci")
}
