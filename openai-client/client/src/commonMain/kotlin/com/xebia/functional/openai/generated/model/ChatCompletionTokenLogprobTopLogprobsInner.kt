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
 * @param token The token.
 * @param logprob The log probability of this token.
 * @param bytes A list of integers representing the UTF-8 bytes representation of the token. Useful
 *   in instances where characters are represented by multiple tokens and their byte representations
 *   must be combined to generate the correct text representation. Can be `null` if there is no
 *   bytes representation for the token.
 */
@Serializable
data class ChatCompletionTokenLogprobTopLogprobsInner(
  /* The token. */
  @SerialName(value = "token") val token: kotlin.String,
  /* The log probability of this token. */
  @SerialName(value = "logprob") val logprob: kotlin.Double,
  /* A list of integers representing the UTF-8 bytes representation of the token. Useful in instances where characters are represented by multiple tokens and their byte representations must be combined to generate the correct text representation. Can be `null` if there is no bytes representation for the token. */
  @SerialName(value = "bytes") val bytes: kotlin.collections.List<kotlin.Int>?
) {}
