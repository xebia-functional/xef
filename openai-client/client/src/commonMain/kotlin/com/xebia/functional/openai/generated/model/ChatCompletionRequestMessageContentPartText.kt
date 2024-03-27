/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * @param type The type of the content part.
 * @param text The text content.
 */
@Serializable
data class ChatCompletionRequestMessageContentPartText(
  /* The type of the content part. */
  @SerialName(value = "type") val type: ChatCompletionRequestMessageContentPartText.Type,
  /* The text content. */
  @SerialName(value = "text") val text: kotlin.String
) {

  /**
   * The type of the content part.
   *
   * Values: text
   */
  @Serializable
  enum class Type(val value: kotlin.String) {
    @SerialName(value = "text") text("text")
  }
}
