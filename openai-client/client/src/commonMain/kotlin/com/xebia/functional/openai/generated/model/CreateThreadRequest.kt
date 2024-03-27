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
 * @param messages A list of [messages](/docs/api-reference/messages) to start the thread with.
 * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful
 *   for storing additional information about the object in a structured format. Keys can be a
 *   maximum of 64 characters long and values can be a maxium of 512 characters long.
 */
@Serializable
data class CreateThreadRequest(
  /* A list of [messages](/docs/api-reference/messages) to start the thread with. */
  @Contextual
  @SerialName(value = "messages")
  val messages: kotlin.collections.List<CreateMessageRequest>? = null,
  /* Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.  */
  @Contextual
  @SerialName(value = "metadata")
  val metadata: kotlinx.serialization.json.JsonObject? = null
) {}
