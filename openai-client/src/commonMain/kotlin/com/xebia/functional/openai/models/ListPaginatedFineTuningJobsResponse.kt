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
 * @param hasMore
 * @param `object`
 */
@Serializable
data class ListPaginatedFineTuningJobsResponse(
  @SerialName(value = "data") @Required val `data`: kotlin.collections.List<FineTuningJob>,
  @SerialName(value = "has_more") @Required val hasMore: kotlin.Boolean,
  @SerialName(value = "object") @Required val `object`: ListPaginatedFineTuningJobsResponse.`Object`
) {

  /** Values: list */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "list") list("list")
  }
}
