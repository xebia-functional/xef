package com.xebia.functional.openai.models.ext.embedding.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateEmbeddingRequestModel(val value: String) {
  @SerialName(value = "text-embedding-ada-002") `text_embedding_ada_002`("text-embedding-ada-002")
}
