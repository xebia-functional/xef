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
 * Represents an embedding vector returned by embedding endpoint.
 *
 * @param index The index of the embedding in the list of embeddings.
 * @param embedding The embedding vector, which is a list of floats. The length of vector depends on
 *   the model as listed in the [embedding guide](/docs/guides/embeddings).
 * @param `object` The object type, which is always \"embedding\".
 */
@Serializable
data class Embedding(
  /* The index of the embedding in the list of embeddings. */
  @SerialName(value = "index") val index: kotlin.Int,
  /* The embedding vector, which is a list of floats. The length of vector depends on the model as listed in the [embedding guide](/docs/guides/embeddings).  */
  @SerialName(value = "embedding")
  val embedding: kotlin.collections.List<@Contextual kotlin.Double>,
  /* The object type, which is always \"embedding\". */
  @SerialName(value = "object") val `object`: Embedding.`Object`
) {

  /**
   * The object type, which is always \"embedding\".
   *
   * Values: embedding
   */
  @Serializable
  enum class `Object`(val value: kotlin.String) {
    @SerialName(value = "embedding") embedding("embedding")
  }
}
