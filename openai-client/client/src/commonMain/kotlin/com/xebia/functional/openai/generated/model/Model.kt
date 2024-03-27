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
 * Describes an OpenAI model offering that can be used with the API.
 *
 * @param id The model identifier, which can be referenced in the API endpoints.
 * @param created The Unix timestamp (in seconds) when the model was created.
 * @param `object` The object type, which is always \"model\".
 * @param ownedBy The organization that owns the model.
 */
@Serializable
data class Model(
  /* The model identifier, which can be referenced in the API endpoints. */
  @SerialName(value = "id") val id: kotlin.String,
  /* The Unix timestamp (in seconds) when the model was created. */
  @SerialName(value = "created") val created: kotlin.Int,
  /* The object type, which is always \"model\". */
  @SerialName(value = "object") val `object`: Model.`Object`,
  /* The organization that owns the model. */
  @SerialName(value = "owned_by") val ownedBy: kotlin.String
) {

  /**
   * The object type, which is always \"model\".
   *
   * Values: model
   */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "model") model("model")
  }
}
