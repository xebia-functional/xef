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
 * @param role The role of the messages author, in this case `tool`.
 * @param content The contents of the tool message.
 * @param toolCallId Tool call that this message is responding to.
 */
@Serializable
data class ChatCompletionRequestToolMessage(
  /* The role of the messages author, in this case `tool`. */
  @SerialName(value = "role") val role: ChatCompletionRequestToolMessage.Role,
  /* The contents of the tool message. */
  @SerialName(value = "content") val content: kotlin.String,
  /* Tool call that this message is responding to. */
  @SerialName(value = "tool_call_id") val toolCallId: kotlin.String
) {

  /**
   * The role of the messages author, in this case `tool`.
   *
   * Values: tool
   */
  @Serializable
  enum class Role(val value: kotlin.String) {
    @SerialName(value = "tool") tool("tool")
  }
}
