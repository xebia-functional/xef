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
 * @param type The type of tool being defined: `function`
 * @param function
 */
@Serializable
data class AssistantToolsFunction(

  /* The type of tool being defined: `function` */
  @SerialName(value = "type") @Required val type: AssistantToolsFunction.Type,
  @SerialName(value = "function") @Required val function: FunctionObject
) {

  /**
   * The type of tool being defined: `function`
   *
   * Values: function
   */
  @Serializable
  enum class Type(val value: kotlin.String) {
    @SerialName(value = "function") function("function")
  }
}
