package com.xebia.functional.openai.models.ext.chat.create

import com.xebia.functional.openai.models.ext.chat.ChatCompletionRequestMessage
import kotlinx.serialization.DeserializationStrategy
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = CreateChatCompletionRequestStop.MyTypeSerializer::class)
sealed interface CreateChatCompletionRequestStop {
  @Serializable @JvmInline value class StringValue(val s: String) : CreateChatCompletionRequestStop

  @Serializable
  @JvmInline
  value class ArrayValue(val array: List<String>) : CreateChatCompletionRequestStop

  object MyTypeSerializer : JsonContentPolymorphicSerializer<CreateChatCompletionRequestStop>(CreateChatCompletionRequestStop::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<CreateChatCompletionRequestStop> = CreateChatCompletionRequestStop.StringValue.serializer()
  }
}

