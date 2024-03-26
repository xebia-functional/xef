/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param model
 * @param input The text to generate audio for. The maximum length is 4096 characters.
 * @param voice The voice to use when generating the audio. Supported voices are `alloy`, `echo`,
 *   `fable`, `onyx`, `nova`, and `shimmer`. Previews of the voices are available in the
 *   [Text to speech guide](/docs/guides/text-to-speech/voice-options).
 * @param responseFormat The format to return audio in. Supported formats are `mp3`, `opus`, `aac`,
 *   `flac`, `pcm`, and `wav`. The `pcm` audio format, similar to `wav` but without a header,
 *   utilizes a 24kHz sample rate, mono channel, and 16-bit depth in signed little-endian format.
 * @param speed The speed of the generated audio. Select a value from `0.25` to `4.0`. `1.0` is the
 *   default.
 */
@Serializable
data class CreateSpeechRequest(
  @SerialName(value = "model") val model: CreateSpeechRequestModel,
  /* The text to generate audio for. The maximum length is 4096 characters. */
  @SerialName(value = "input") val input: kotlin.String,
  /* The voice to use when generating the audio. Supported voices are `alloy`, `echo`, `fable`, `onyx`, `nova`, and `shimmer`. Previews of the voices are available in the [Text to speech guide](/docs/guides/text-to-speech/voice-options). */
  @SerialName(value = "voice") val voice: CreateSpeechRequest.Voice,
  /* The format to return audio in.  Supported formats are `mp3`, `opus`, `aac`, `flac`, `pcm`, and `wav`.   The `pcm` audio format, similar to `wav` but without a header, utilizes a 24kHz sample rate, mono channel, and 16-bit depth in signed little-endian format. */
  @Contextual
  @SerialName(value = "response_format")
  val responseFormat: CreateSpeechRequest.ResponseFormat? = ResponseFormat.mp3,
  /* The speed of the generated audio. Select a value from `0.25` to `4.0`. `1.0` is the default. */
  @Contextual @SerialName(value = "speed") val speed: kotlin.Double? = (1.0).toDouble()
) {

  /**
   * The voice to use when generating the audio. Supported voices are `alloy`, `echo`, `fable`,
   * `onyx`, `nova`, and `shimmer`. Previews of the voices are available in the
   * [Text to speech guide](/docs/guides/text-to-speech/voice-options).
   *
   * Values: alloy,echo,fable,onyx,nova,shimmer
   */
  @Serializable
  enum class Voice(val value: kotlin.String) {
    @SerialName(value = "alloy") alloy("alloy"),
    @SerialName(value = "echo") echo("echo"),
    @SerialName(value = "fable") fable("fable"),
    @SerialName(value = "onyx") onyx("onyx"),
    @SerialName(value = "nova") nova("nova"),
    @SerialName(value = "shimmer") shimmer("shimmer")
  }
  /**
   * The format to return audio in. Supported formats are `mp3`, `opus`, `aac`, `flac`, `pcm`, and
   * `wav`. The `pcm` audio format, similar to `wav` but without a header, utilizes a 24kHz sample
   * rate, mono channel, and 16-bit depth in signed little-endian format.
   *
   * Values: mp3,opus,aac,flac,pcm,wav
   */
  @Serializable
  enum class ResponseFormat(val value: kotlin.String) {
    @SerialName(value = "mp3") mp3("mp3"),
    @SerialName(value = "opus") opus("opus"),
    @SerialName(value = "aac") aac("aac"),
    @SerialName(value = "flac") flac("flac"),
    @SerialName(value = "pcm") pcm("pcm"),
    @SerialName(value = "wav") wav("wav")
  }
}
