/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlin.js.JsName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param finishReason The reason the model stopped generating tokens. This will be `stop` if the
 *   model hit a natural stop point or a provided stop sequence, `length` if the maximum number of
 *   tokens specified in the request was reached, `content_filter` if content was omitted due to a
 *   flag from our content filters, or `function_call` if the model called a function.
 * @param index The index of the choice in the list of choices.
 * @param message
 */
@Serializable
data class CreateChatCompletionFunctionResponseChoicesInner(
  /* The reason the model stopped generating tokens. This will be `stop` if the model hit a natural stop point or a provided stop sequence, `length` if the maximum number of tokens specified in the request was reached, `content_filter` if content was omitted due to a flag from our content filters, or `function_call` if the model called a function.  */
  @SerialName(value = "finish_reason")
  val finishReason: CreateChatCompletionFunctionResponseChoicesInner.FinishReason,
  /* The index of the choice in the list of choices. */
  @SerialName(value = "index") val index: kotlin.Int,
  @SerialName(value = "message") val message: ChatCompletionResponseMessage
) {

  /**
   * The reason the model stopped generating tokens. This will be `stop` if the model hit a natural
   * stop point or a provided stop sequence, `length` if the maximum number of tokens specified in
   * the request was reached, `content_filter` if content was omitted due to a flag from our content
   * filters, or `function_call` if the model called a function.
   *
   * Values: stop,length,function_call,content_filter
   */
  @Serializable
  enum class FinishReason(val value: kotlin.String) {
    @SerialName(value = "stop") stop("stop"),
    @SerialName(value = "length") @JsName("length_type") length("length"),
    @SerialName(value = "function_call") function_call("function_call"),
    @SerialName(value = "content_filter") content_filter("content_filter")
  }
}
