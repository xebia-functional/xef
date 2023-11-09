package com.xebia.functional.openai.models.ext.speech.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateSpeechRequestModel(val value: String) {
  @SerialName(value = "tts-1") `tts_1`("tts-1"),
  @SerialName(value = "tts-1-hd") `tts_1_hd`("tts-1-hd")
}
