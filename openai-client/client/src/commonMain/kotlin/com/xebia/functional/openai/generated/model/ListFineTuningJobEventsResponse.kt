/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param `data`
 * @param `object`
 */
@Serializable
data class ListFineTuningJobEventsResponse(
  @SerialName(value = "data") val `data`: kotlin.collections.List<FineTuningJobEvent>,
  @SerialName(value = "object") val `object`: ListFineTuningJobEventsResponse.`Object`
) {

  /** Values: list */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "list") list("list")
  }
}
