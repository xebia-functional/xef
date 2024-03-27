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
 * @param id The ID of the tool call object.
 * @param type The type of tool call. This is always going to be `function` for this type of tool
 *   call.
 * @param function
 */
@Serializable
data class RunStepDetailsToolCallsFunctionObject(
  /* The ID of the tool call object. */
  @SerialName(value = "id") val id: kotlin.String,
  /* The type of tool call. This is always going to be `function` for this type of tool call. */
  @SerialName(value = "type") val type: RunStepDetailsToolCallsFunctionObject.Type,
  @SerialName(value = "function") val function: RunStepDetailsToolCallsFunctionObjectFunction
) {

  /**
   * The type of tool call. This is always going to be `function` for this type of tool call.
   *
   * Values: function
   */
  @Serializable
  enum class Type(val value: kotlin.String) {
    @SerialName(value = "function") function("function")
  }
}
