package com.xebia.functional.openai.models.ext.transcription.create

import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateTranscriptionRequestModel(val value: String) {
  @SerialName(value = "whisper-1") `whisper_1`("whisper-1");

  fun asByteArray(): ByteArray = this.value.toByteArray(Charsets.UTF_8)
}
