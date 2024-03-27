/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

/**
 * @param name The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores
 *   and dashes, with a maximum length of 64.
 * @param description A description of what the function does, used by the model to choose when and
 *   how to call the function.
 * @param parameters
 */
@Serializable
@Deprecated(message = "This schema is deprecated.")
data class ChatCompletionFunctions(
  /* The name of the function to be called. Must be a-z, A-Z, 0-9, or contain underscores and dashes, with a maximum length of 64. */
  @SerialName(value = "name") val name: kotlin.String,
  /* A description of what the function does, used by the model to choose when and how to call the function. */
  @Contextual @SerialName(value = "description") val description: kotlin.String? = null,
  @Contextual
  @SerialName(value = "parameters")
  val parameters: kotlinx.serialization.json.JsonObject? = null
) {}
