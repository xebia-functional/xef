/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.models

import kotlinx.serialization.*

/**
 * ID of the model to use. See the
 * [model endpoint compatibility](/docs/models/model-endpoint-compatibility) table for details on
 * which models work with the Chat API.
 *
 * Values:
 * gptMinus4Minus1106MinusPreview,gptMinus4MinusVisionMinusPreview,gptMinus4,gptMinus4Minus0314,gptMinus4Minus0613,gptMinus4Minus32k,gptMinus4Minus32kMinus0314,gptMinus4Minus32kMinus0613,gptMinus3Period5MinusTurbo,gptMinus3Period5MinusTurboMinus16k,gptMinus3Period5MinusTurboMinus0301,gptMinus3Period5MinusTurboMinus0613,gptMinus3Period5MinusTurboMinus16kMinus0613
 */
@Serializable
enum class CreateChatCompletionRequestModel(val value: kotlin.String) {

  @SerialName(value = "gpt-4-1106-preview") gptMinus4Minus1106MinusPreview("gpt-4-1106-preview"),
  @SerialName(value = "gpt-4-vision-preview")
  gptMinus4MinusVisionMinusPreview("gpt-4-vision-preview"),
  @SerialName(value = "gpt-4") gptMinus4("gpt-4"),
  @SerialName(value = "gpt-4-0314") gptMinus4Minus0314("gpt-4-0314"),
  @SerialName(value = "gpt-4-0613") gptMinus4Minus0613("gpt-4-0613"),
  @SerialName(value = "gpt-4-32k") gptMinus4Minus32k("gpt-4-32k"),
  @SerialName(value = "gpt-4-32k-0314") gptMinus4Minus32kMinus0314("gpt-4-32k-0314"),
  @SerialName(value = "gpt-4-32k-0613") gptMinus4Minus32kMinus0613("gpt-4-32k-0613"),
  @SerialName(value = "gpt-3.5-turbo") gptMinus3Period5MinusTurbo("gpt-3.5-turbo"),
  @SerialName(value = "gpt-3.5-turbo-16k") gptMinus3Period5MinusTurboMinus16k("gpt-3.5-turbo-16k"),
  @SerialName(value = "gpt-3.5-turbo-0301")
  gptMinus3Period5MinusTurboMinus0301("gpt-3.5-turbo-0301"),
  @SerialName(value = "gpt-3.5-turbo-0613")
  gptMinus3Period5MinusTurboMinus0613("gpt-3.5-turbo-0613"),
  @SerialName(value = "gpt-3.5-turbo-16k-0613")
  gptMinus3Period5MinusTurboMinus16kMinus0613("gpt-3.5-turbo-16k-0613");

  /**
   * Override [toString()] to avoid using the enum variable name as the value, and instead use the
   * actual value defined in the API spec file.
   *
   * This solves a problem when the variable name and its value are different, and ensures that the
   * client sends the correct enum values to the server always.
   */
  override fun toString(): kotlin.String = value

  companion object {
    /** Converts the provided [data] to a [String] on success, null otherwise. */
    fun encode(data: kotlin.Any?): kotlin.String? =
      if (data is CreateChatCompletionRequestModel) "$data" else null

    /** Returns a valid [CreateChatCompletionRequestModel] for [data], null otherwise. */
    fun decode(data: kotlin.Any?): CreateChatCompletionRequestModel? =
      data?.let {
        val normalizedData = "$it".lowercase()
        values().firstOrNull { value -> it == value || normalizedData == "$value".lowercase() }
      }
  }
}