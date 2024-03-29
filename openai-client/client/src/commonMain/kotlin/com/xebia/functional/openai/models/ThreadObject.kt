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
 * Represents a thread that contains [messages](/docs/api-reference/messages).
 *
 * @param id The identifier, which can be referenced in API endpoints.
 * @param `object` The object type, which is always `thread`.
 * @param createdAt The Unix timestamp (in seconds) for when the thread was created.
 * @param metadata Set of 16 key-value pairs that can be attached to an object. This can be useful
 *   for storing additional information about the object in a structured format. Keys can be a
 *   maximum of 64 characters long and values can be a maxium of 512 characters long.
 */
@Serializable
data class ThreadObject(

  /* The identifier, which can be referenced in API endpoints. */
  @SerialName(value = "id") @Required val id: kotlin.String,

  /* The object type, which is always `thread`. */
  @SerialName(value = "object") @Required val `object`: ThreadObject.`Object`,

  /* The Unix timestamp (in seconds) for when the thread was created. */
  @SerialName(value = "created_at") @Required val createdAt: kotlin.Int,

  /* Set of 16 key-value pairs that can be attached to an object. This can be useful for storing additional information about the object in a structured format. Keys can be a maximum of 64 characters long and values can be a maxium of 512 characters long.  */
  @SerialName(value = "metadata") @Required val metadata: kotlinx.serialization.json.JsonObject?
) {

  /**
   * The object type, which is always `thread`.
   *
   * Values: thread
   */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "thread") thread("thread")
  }
}
