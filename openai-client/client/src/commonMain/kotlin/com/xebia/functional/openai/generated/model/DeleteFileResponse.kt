/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param id
 * @param `object`
 * @param deleted
 */
@Serializable
data class DeleteFileResponse(
  @SerialName(value = "id") val id: kotlin.String,
  @SerialName(value = "object") val `object`: DeleteFileResponse.`Object`,
  @SerialName(value = "deleted") val deleted: kotlin.Boolean
) {

  /** Values: file */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "file") file("file")
  }
}
