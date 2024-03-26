/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param `data` The list of embeddings generated by the model.
 * @param model The name of the model used to generate the embedding.
 * @param `object` The object type, which is always \"list\".
 * @param usage
 */
@Serializable
data class CreateEmbeddingResponse(
  /* The list of embeddings generated by the model. */
  @SerialName(value = "data") val `data`: kotlin.collections.List<Embedding>,
  /* The name of the model used to generate the embedding. */
  @SerialName(value = "model") val model: kotlin.String,
  /* The object type, which is always \"list\". */
  @SerialName(value = "object") val `object`: CreateEmbeddingResponse.`Object`,
  @SerialName(value = "usage") val usage: CreateEmbeddingResponseUsage
) {

  /**
   * The object type, which is always \"list\".
   *
   * Values: list
   */
  @Serializable
  enum class `Object`(name: kotlin.String) {
    @SerialName(value = "list") list("list")
  }
}
