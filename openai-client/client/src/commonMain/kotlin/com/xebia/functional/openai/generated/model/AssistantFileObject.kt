/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A list of [Files](/docs/api-reference/files) attached to an `assistant`.
 *
 * @param id The identifier, which can be referenced in API endpoints.
 * @param `object` The object type, which is always `assistant.file`.
 * @param createdAt The Unix timestamp (in seconds) for when the assistant file was created.
 * @param assistantId The assistant ID that the file is attached to.
 */
@Serializable
data class AssistantFileObject(
  /* The identifier, which can be referenced in API endpoints. */
  @SerialName(value = "id") val id: kotlin.String,
  /* The object type, which is always `assistant.file`. */
  @SerialName(value = "object") val `object`: AssistantFileObject.`Object`,
  /* The Unix timestamp (in seconds) for when the assistant file was created. */
  @SerialName(value = "created_at") val createdAt: kotlin.Int,
  /* The assistant ID that the file is attached to. */
  @SerialName(value = "assistant_id") val assistantId: kotlin.String
) {

  /**
   * The object type, which is always `assistant.file`.
   *
   * Values: assistant_file
   */
  @Serializable
  enum class `Object`(name: kotlin.String) {
    @SerialName(value = "assistant.file") assistant_file("assistant.file")
  }
}
