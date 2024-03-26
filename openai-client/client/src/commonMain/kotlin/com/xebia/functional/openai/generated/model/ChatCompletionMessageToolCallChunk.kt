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
 * @param index
 * @param id The ID of the tool call.
 * @param type The type of the tool. Currently, only `function` is supported.
 * @param function
 */
@Serializable
data class ChatCompletionMessageToolCallChunk(
  @SerialName(value = "index") val index: kotlin.Int,
  /* The ID of the tool call. */
  @Contextual @SerialName(value = "id") val id: kotlin.String? = null,
  /* The type of the tool. Currently, only `function` is supported. */
  @Contextual @SerialName(value = "type") val type: ChatCompletionMessageToolCallChunk.Type? = null,
  @Contextual
  @SerialName(value = "function")
  val function: ChatCompletionMessageToolCallChunkFunction? = null
) {

  /**
   * The type of the tool. Currently, only `function` is supported.
   *
   * Values: function
   */
  @Serializable
  enum class Type(val value: kotlin.String) {
    @SerialName(value = "function") function("function")
  }
}
