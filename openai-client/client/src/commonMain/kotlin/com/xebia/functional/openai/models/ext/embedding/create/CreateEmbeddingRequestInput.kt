package com.xebia.functional.openai.models.ext.embedding.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

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

  object MyTypeSerializer :
    JsonContentPolymorphicSerializer<CreateEmbeddingRequestInput>(
      CreateEmbeddingRequestInput::class
    ) {
    override fun selectDeserializer(
      element: JsonElement
    ): DeserializationStrategy<CreateEmbeddingRequestInput> =
      if (element is JsonArray) {
        val firstElement = element.firstOrNull()
        if (firstElement == null) {
          StringArrayValue.serializer()
        } else if (firstElement is JsonArray) {
          IntArrayArrayValue.serializer()
        } else if (firstElement.jsonPrimitive.isString) {
          StringArrayValue.serializer()
        } else {
          IntArrayValue.serializer()
        }
      } else StringValue.serializer()
  }
}
