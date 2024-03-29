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
 * Usage statistics related to the run step. This value will be `null` while the run step's status
 * is `in_progress`.
 *
 * @param completionTokens Number of completion tokens used over the course of the run step.
 * @param promptTokens Number of prompt tokens used over the course of the run step.
 * @param totalTokens Total number of tokens used (prompt + completion).
 */
@Serializable
data class RunStepCompletionUsage(

  /* Number of completion tokens used over the course of the run step. */
  @SerialName(value = "completion_tokens") @Required val completionTokens: kotlin.Int,

  /* Number of prompt tokens used over the course of the run step. */
  @SerialName(value = "prompt_tokens") @Required val promptTokens: kotlin.Int,

  /* Total number of tokens used (prompt + completion). */
  @SerialName(value = "total_tokens") @Required val totalTokens: kotlin.Int
)
