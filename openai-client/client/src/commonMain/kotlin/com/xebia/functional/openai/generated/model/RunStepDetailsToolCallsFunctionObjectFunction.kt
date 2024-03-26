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
 * The definition of the function that was called.
 *
 * @param name The name of the function.
 * @param arguments The arguments passed to the function.
 * @param output The output of the function. This will be `null` if the outputs have not been
 *   [submitted](/docs/api-reference/runs/submitToolOutputs) yet.
 */
@Serializable
data class RunStepDetailsToolCallsFunctionObjectFunction(
  /* The name of the function. */
  @SerialName(value = "name") val name: kotlin.String,
  /* The arguments passed to the function. */
  @SerialName(value = "arguments") val arguments: kotlin.String,
  /* The output of the function. This will be `null` if the outputs have not been [submitted](/docs/api-reference/runs/submitToolOutputs) yet. */
  @Contextual @SerialName(value = "output") val output: kotlin.String? = null
)
