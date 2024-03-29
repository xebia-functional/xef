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
  @SerialName(value = "index") @Required val index: kotlin.Int,

  /* The embedding vector, which is a list of floats. The length of vector depends on the model as listed in the [embedding guide](/docs/guides/embeddings).  */
  @SerialName(value = "embedding") @Required val embedding: kotlin.collections.List<kotlin.Double>,

  /* The object type, which is always \"embedding\". */
  @SerialName(value = "object") @Required val `object`: Embedding.`Object`
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
