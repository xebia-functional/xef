/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Details of the Code Interpreter tool call the run step was involved in.
 *
 * @param id The ID of the tool call.
 * @param type The type of tool call. This is always going to be `code_interpreter` for this type of
 *   tool call.
 * @param codeInterpreter
 */
@Serializable
data class RunStepDetailsToolCallsCodeObject(
  /* The ID of the tool call. */
  @SerialName(value = "id") val id: kotlin.String,
  /* The type of tool call. This is always going to be `code_interpreter` for this type of tool call. */
  @SerialName(value = "type") val type: RunStepDetailsToolCallsCodeObject.Type,
  @SerialName(value = "code_interpreter")
  val codeInterpreter: RunStepDetailsToolCallsCodeObjectCodeInterpreter
) {

  /**
   * The type of tool call. This is always going to be `code_interpreter` for this type of tool
   * call.
   *
   * Values: code_interpreter
   */
  @Serializable
  enum class Type(name: kotlin.String) {
    @SerialName(value = "code_interpreter") code_interpreter("code_interpreter")
  }
}
