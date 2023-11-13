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
 * @param finishReason The reason the model stopped generating tokens. This will be `stop` if the
 *   model hit a natural stop point or a provided stop sequence, `length` if the maximum number of
 *   tokens specified in the request was reached, or `content_filter` if content was omitted due to
 *   a flag from our content filters.
 * @param index The index of the choice in the list of choices.
 * @param text The edited result.
 */
@Serializable
data class CreateEditResponseChoicesInner(

  /* The reason the model stopped generating tokens. This will be `stop` if the model hit a natural stop point or a provided stop sequence, `length` if the maximum number of tokens specified in the request was reached, or `content_filter` if content was omitted due to a flag from our content filters.  */
  @SerialName(value = "finish_reason")
  @Required
  val finishReason: CreateEditResponseChoicesInner.FinishReason,

  /* The index of the choice in the list of choices. */
  @SerialName(value = "index") @Required val index: kotlin.Int,

  /* The edited result. */
  @SerialName(value = "text") @Required val text: kotlin.String
) {

  /**
   * The reason the model stopped generating tokens. This will be `stop` if the model hit a natural
   * stop point or a provided stop sequence, `length` if the maximum number of tokens specified in
   * the request was reached, or `content_filter` if content was omitted due to a flag from our
   * content filters.
   *
   * Values: stop,lengthType
   */
  @Serializable
  enum class FinishReason(val value: kotlin.String) {
    @SerialName(value = "stop") stop("stop"),
    @SerialName(value = "length") lengthType("length")
  }
}