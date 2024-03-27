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
 * A URL for the file that's generated when the assistant used the `code_interpreter` tool to
 * generate a file.
 *
 * @param type Always `file_path`.
 * @param text The text in the message content that needs to be replaced.
 * @param filePath
 * @param startIndex
 * @param endIndex
 */
@Serializable
data class MessageContentTextAnnotationsFilePathObject(
  /* Always `file_path`. */
  @SerialName(value = "type") val type: MessageContentTextAnnotationsFilePathObject.Type,
  /* The text in the message content that needs to be replaced. */
  @SerialName(value = "text") val text: kotlin.String,
  @SerialName(value = "file_path")
  val filePath: MessageContentTextAnnotationsFilePathObjectFilePath,
  @SerialName(value = "start_index") val startIndex: kotlin.Int,
  @SerialName(value = "end_index") val endIndex: kotlin.Int
) {

  /**
   * Always `file_path`.
   *
   * Values: file_path
   */
  @Serializable
  enum class Type(val value: kotlin.String) {
    @SerialName(value = "file_path") file_path("file_path")
  }
}
