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
 * The function that the model called.
 *
 * @param name The name of the function to call.
 * @param arguments The arguments to call the function with, as generated by the model in JSON
 *   format. Note that the model does not always generate valid JSON, and may hallucinate parameters
 *   not defined by your function schema. Validate the arguments in your code before calling your
 *   function.
 */
@Serializable
data class ChatCompletionMessageToolCallFunction(
  /* The name of the function to call. */
  @SerialName(value = "name") val name: kotlin.String,
  /* The arguments to call the function with, as generated by the model in JSON format. Note that the model does not always generate valid JSON, and may hallucinate parameters not defined by your function schema. Validate the arguments in your code before calling your function. */
  @SerialName(value = "arguments") val arguments: kotlin.String
) {}
