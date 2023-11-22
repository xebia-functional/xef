package com.xebia.functional.openai.models.ext.chat

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with=ChatCompletionRequestUserMessageContent.MyTypeSerializer::class)
sealed class ChatCompletionRequestUserMessageContent {

  object MyTypeSerializer : JsonContentPolymorphicSerializer<ChatCompletionRequestUserMessageContent>(ChatCompletionRequestUserMessageContent::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ChatCompletionRequestUserMessageContent> = ChatCompletionRequestUserMessageContentText.serializer()
  }

}
