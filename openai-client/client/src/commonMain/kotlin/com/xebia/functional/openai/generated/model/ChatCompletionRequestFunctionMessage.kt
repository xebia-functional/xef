/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param role The role of the messages author, in this case `function`.
 * @param content The contents of the function message.
 * @param name The name of the function to call.
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")
data class ChatCompletionRequestFunctionMessage(
  /* The role of the messages author, in this case `function`. */
  @SerialName(value = "role") val role: ChatCompletionRequestFunctionMessage.Role,
  /* The contents of the function message. */
  @SerialName(value = "content") val content: kotlin.String?,
  /* The name of the function to call. */
  @SerialName(value = "name") val name: kotlin.String
) {

  /**
   * The role of the messages author, in this case `function`.
   *
   * Values: function
   */
  @Serializable
  enum class Role(val value: kotlin.String) {
    @SerialName(value = "function") function("function")
  }
}
