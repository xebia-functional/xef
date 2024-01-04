package com.xebia.functional.openai.models.ext.chat.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = CreateChatCompletionRequestStop.MyTypeSerializer::class)
sealed interface CreateChatCompletionRequestStop {
  @Serializable @JvmInline value class StringValue(val s: String) : CreateChatCompletionRequestStop

  @Serializable
  @JvmInline
  value class ArrayValue(val array: List<String>) : CreateChatCompletionRequestStop

  object MyTypeSerializer :
    JsonContentPolymorphicSerializer<CreateChatCompletionRequestStop>(
      CreateChatCompletionRequestStop::class
    ) {
    override fun selectDeserializer(
      element: JsonElement
    ): DeserializationStrategy<CreateChatCompletionRequestStop> =
      if (element is JsonArray) ArrayValue.serializer() else StringValue.serializer()
  }
}
