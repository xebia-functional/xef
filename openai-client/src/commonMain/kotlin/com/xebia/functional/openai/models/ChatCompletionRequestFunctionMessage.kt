/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.models

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * @param role The role of the messages author, in this case `function`.
 * @param content The return value from the function call, to return to the model.
 * @param name The name of the function to call.
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")
data class ChatCompletionRequestFunctionMessage(

  /* The role of the messages author, in this case `function`. */
  @SerialName(value = "role") @Required val role: ChatCompletionRequestFunctionMessage.Role,

  /* The return value from the function call, to return to the model. */
  @SerialName(value = "content") @Required val content: kotlin.String?,

  /* The name of the function to call. */
  @SerialName(value = "name") @Required val name: kotlin.String
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
