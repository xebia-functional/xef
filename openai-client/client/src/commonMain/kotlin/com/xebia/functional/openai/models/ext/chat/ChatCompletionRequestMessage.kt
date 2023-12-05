package com.xebia.functional.openai.models.ext.chat

import com.xebia.functional.openai.models.ChatCompletionRole
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

@Serializable(with = ChatCompletionRequestMessage.MyTypeSerializer::class)
sealed interface ChatCompletionRequestMessage {

  fun contentAsString(): String =
    when (this) {
      is ChatCompletionRequestAssistantMessage -> content ?: ""
      is ChatCompletionRequestFunctionMessage -> content ?: ""
      is ChatCompletionRequestSystemMessage -> content ?: ""
      is ChatCompletionRequestToolMessage -> content ?: ""
      is ChatCompletionRequestUserMessage ->
        content.joinToString { content ->
          when (content) {
            is ChatCompletionRequestUserMessageContentText -> content.text
            is ChatCompletionRequestUserMessageContentImage -> content.imageUrl.url ?: ""
          }
        }
    }

  fun completionRole(): ChatCompletionRole =
    when (this) {
      is ChatCompletionRequestAssistantMessage -> role.asRole
      is ChatCompletionRequestFunctionMessage -> role.asRole
      is ChatCompletionRequestSystemMessage -> role.asRole
      is ChatCompletionRequestToolMessage -> role.asRole
      is ChatCompletionRequestUserMessage -> role.asRole
    }

  object MyTypeSerializer :
    JsonContentPolymorphicSerializer<ChatCompletionRequestMessage>(
      ChatCompletionRequestMessage::class
    ) {
    override fun selectDeserializer(
      element: JsonElement
    ): DeserializationStrategy<ChatCompletionRequestMessage> =
      when (element.jsonObject["role"]?.jsonPrimitive?.contentOrNull) {
        ChatCompletionRole.assistant.value -> ChatCompletionRequestAssistantMessage.serializer()
        ChatCompletionRole.function.value -> ChatCompletionRequestFunctionMessage.serializer()
        ChatCompletionRole.system.value -> ChatCompletionRequestSystemMessage.serializer()
        ChatCompletionRole.tool.value -> ChatCompletionRequestToolMessage.serializer()
        else -> ChatCompletionRequestUserMessage.serializer()
      }
  }
}
