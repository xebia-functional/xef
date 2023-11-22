package com.xebia.functional.openai.models.ext.embedding.create

import kotlinx.serialization.DeserializationStrategy
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = CreateEmbeddingRequestInput.MyTypeSerializer::class)
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

  object MyTypeSerializer : JsonContentPolymorphicSerializer<CreateEmbeddingRequestInput>(CreateEmbeddingRequestInput::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CreateEmbeddingRequestInput> = StringValue.serializer()
  }
}
