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
 * Usage statistics related to the run. This value will be `null` if the run is not in a terminal
 * state (i.e. `in_progress`, `queued`, etc.).
 *
 * @param completionTokens Number of completion tokens used over the course of the run.
 * @param promptTokens Number of prompt tokens used over the course of the run.
 * @param totalTokens Total number of tokens used (prompt + completion).
 */
@Serializable
data class RunCompletionUsage(
  /* Number of completion tokens used over the course of the run. */
  @SerialName(value = "completion_tokens") val completionTokens: kotlin.Int,
  /* Number of prompt tokens used over the course of the run. */
  @SerialName(value = "prompt_tokens") val promptTokens: kotlin.Int,
  /* Total number of tokens used (prompt + completion). */
  @SerialName(value = "total_tokens") val totalTokens: kotlin.Int
) {}
