package com.xebia.functional.openai.models.ext.embedding.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateEmbeddingRequestInput {

  @Serializable @JvmInline value class StringValue(val v: String) : CreateEmbeddingRequestInput

  @Serializable
  @JvmInline
  value class StringArrayValue(val v: List<String>) : CreateEmbeddingRequestInput

  @Serializable
  @JvmInline
  value class IntArrayValue(val v: List<Int>) : CreateEmbeddingRequestInput

  @Serializable
  @JvmInline
  value class IntArrayArrayValue(val v: List<List<Int>>) : CreateEmbeddingRequestInput
}
