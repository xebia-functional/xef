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
 * Represents a streamed chunk of a chat completion response returned by model, based on the
 * provided input.
 *
 * @param id A unique identifier for the chat completion. Each chunk has the same ID.
 * @param choices A list of chat completion choices. Can be more than one if `n` is greater than 1.
 * @param created The Unix timestamp (in seconds) of when the chat completion was created. Each
 *   chunk has the same timestamp.
 * @param model The model to generate the completion.
 * @param `object` The object type, which is always `chat.completion.chunk`.
 * @param systemFingerprint This fingerprint represents the backend configuration that the model
 *   runs with. Can be used in conjunction with the `seed` request parameter to understand when
 *   backend changes have been made that might impact determinism.
 */
@Serializable
data class CreateChatCompletionStreamResponse(

  /* A unique identifier for the chat completion. Each chunk has the same ID. */
  @SerialName(value = "id") @Required val id: kotlin.String,

  /* A list of chat completion choices. Can be more than one if `n` is greater than 1. */
  @SerialName(value = "choices")
  @Required
  val choices: kotlin.collections.List<CreateChatCompletionStreamResponseChoicesInner>,

  /* The Unix timestamp (in seconds) of when the chat completion was created. Each chunk has the same timestamp. */
  @SerialName(value = "created") @Required val created: kotlin.Int,

  /* The model to generate the completion. */
  @SerialName(value = "model") @Required val model: kotlin.String,

  /* The object type, which is always `chat.completion.chunk`. */
  @SerialName(value = "object") @Required val `object`: CreateChatCompletionStreamResponse.`Object`,

  /* This fingerprint represents the backend configuration that the model runs with. Can be used in conjunction with the `seed` request parameter to understand when backend changes have been made that might impact determinism.  */
  @SerialName(value = "system_fingerprint") val systemFingerprint: kotlin.String? = null
) {

  /**
   * The object type, which is always `chat.completion.chunk`.
   *
   * Values: chat_completion_chunk
   */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "chat.completion.chunk") chat_completion_chunk("chat.completion.chunk")
  }
}
