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
 * @param `data`
 * @param `object`
 */
@Serializable
data class ListFineTuningJobEventsResponse(
  @SerialName(value = "data") @Required val `data`: kotlin.collections.List<FineTuningJobEvent>,
  @SerialName(value = "object") @Required val `object`: ListFineTuningJobEventsResponse.`Object`
) {

  /** Values: list */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "list") list("list")
  }
}