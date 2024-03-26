/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * An object specifying the format that the model must output. Compatible with
 * [GPT-4 Turbo](/docs/models/gpt-4-and-gpt-4-turbo) and all GPT-3.5 Turbo models newer than
 * `gpt-3.5-turbo-1106`. Setting to `{ \"type\": \"json_object\" }` enables JSON mode, which
 * guarantees the message the model generates is valid JSON. **Important:** when using JSON mode,
 * you **must** also instruct the model to produce JSON yourself via a system or user message.
 * Without this, the model may generate an unending stream of whitespace until the generation
 * reaches the token limit, resulting in a long-running and seemingly \"stuck\" request. Also note
 * that the message content may be partially cut off if `finish_reason=\"length\"`, which indicates
 * the generation exceeded `max_tokens` or the conversation exceeded the max context length.
 *
 * @param type Must be one of `text` or `json_object`.
 */
@Serializable
data class CreateChatCompletionRequestResponseFormat(
  /* Must be one of `text` or `json_object`. */
  @SerialName(value = "type") val type: CreateChatCompletionRequestResponseFormat.Type? = Type.text
) {

  /**
   * Must be one of `text` or `json_object`.
   *
   * Values: text,json_object
   */
  @Serializable
  enum class Type(name: kotlin.String) {
    @SerialName(value = "text") text("text"),
    @SerialName(value = "json_object") json_object("json_object")
  }
}
