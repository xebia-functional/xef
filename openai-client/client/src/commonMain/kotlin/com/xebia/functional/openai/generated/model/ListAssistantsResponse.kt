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
 * @param `object`
 * @param `data`
 * @param firstId
 * @param lastId
 * @param hasMore
 */
@Serializable
data class ListAssistantsResponse(
  @SerialName(value = "object") val `object`: kotlin.String,
  @SerialName(value = "data") val `data`: kotlin.collections.List<AssistantObject>,
  @Contextual @SerialName(value = "first_id") val firstId: kotlin.String? = null,
  @Contextual @SerialName(value = "last_id") val lastId: kotlin.String? = null,
  @SerialName(value = "has_more") val hasMore: kotlin.Boolean
) {}
