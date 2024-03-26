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
 * @param textOffset
 * @param tokenLogprobs
 * @param tokens
 * @param topLogprobs
 */
@Serializable
data class CreateCompletionResponseChoicesInnerLogprobs(
  @Contextual
  @SerialName(value = "text_offset")
  val textOffset: kotlin.collections.List<kotlin.Int>? = null,
  @Contextual
  @SerialName(value = "token_logprobs")
  val tokenLogprobs: kotlin.collections.List<@Contextual kotlin.Double>? = null,
  @Contextual
  @SerialName(value = "tokens")
  val tokens: kotlin.collections.List<kotlin.String>? = null,
  @Contextual
  @SerialName(value = "top_logprobs")
  val topLogprobs:
    kotlin.collections.List<@Contextual kotlin.collections.Map<kotlin.String, kotlin.Double>>? =
    null
)
