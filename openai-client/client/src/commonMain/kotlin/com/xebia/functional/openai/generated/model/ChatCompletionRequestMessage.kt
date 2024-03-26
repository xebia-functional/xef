/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import kotlin.jvm.JvmInline
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable(with = ChatCompletionRequestMessageSerializer::class)
sealed interface ChatCompletionRequestMessage {

  @JvmInline
  @Serializable
  value class First(val value: ChatCompletionRequestAssistantMessage) :
    ChatCompletionRequestMessage

  @JvmInline
  @Serializable
  value class Second(val value: ChatCompletionRequestFunctionMessage) :
    ChatCompletionRequestMessage

  @JvmInline
  @Serializable
  value class Third(val value: ChatCompletionRequestSystemMessage) : ChatCompletionRequestMessage

  @JvmInline
  @Serializable
  value class Fourth(val value: ChatCompletionRequestToolMessage) : ChatCompletionRequestMessage

  @JvmInline
  @Serializable
  value class Fifth(val value: ChatCompletionRequestUserMessage) : ChatCompletionRequestMessage
}

private object ChatCompletionRequestMessageSerializer : KSerializer<ChatCompletionRequestMessage> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("ChatCompletionRequestMessage", PolymorphicKind.SEALED) {
      element("First", ChatCompletionRequestAssistantMessage.serializer().descriptor)
      element("Second", ChatCompletionRequestFunctionMessage.serializer().descriptor)
      element("Third", ChatCompletionRequestSystemMessage.serializer().descriptor)
      element("Fourth", ChatCompletionRequestToolMessage.serializer().descriptor)
      element("Fifth", ChatCompletionRequestUserMessage.serializer().descriptor)
    }

  override fun deserialize(decoder: Decoder): ChatCompletionRequestMessage =
    kotlin
      .runCatching {
        ChatCompletionRequestMessage.First(
          ChatCompletionRequestAssistantMessage.serializer().deserialize(decoder)
        )
      }
      .getOrNull()
      ?: kotlin
        .runCatching {
          ChatCompletionRequestMessage.Second(
            ChatCompletionRequestFunctionMessage.serializer().deserialize(decoder)
          )
        }
        .getOrNull()
      ?: kotlin
        .runCatching {
          ChatCompletionRequestMessage.Third(
            ChatCompletionRequestSystemMessage.serializer().deserialize(decoder)
          )
        }
        .getOrNull()
      ?: kotlin
        .runCatching {
          ChatCompletionRequestMessage.Fourth(
            ChatCompletionRequestToolMessage.serializer().deserialize(decoder)
          )
        }
        .getOrNull()
      ?: kotlin
        .runCatching {
          ChatCompletionRequestMessage.Fifth(
            ChatCompletionRequestUserMessage.serializer().deserialize(decoder)
          )
        }
        .getOrThrow()

  override fun serialize(encoder: Encoder, value: ChatCompletionRequestMessage) =
    when (value) {
      is ChatCompletionRequestMessage.First ->
        encoder.encodeSerializableValue(
          ChatCompletionRequestAssistantMessage.serializer(),
          value.value
        )
      is ChatCompletionRequestMessage.Second ->
        encoder.encodeSerializableValue(
          ChatCompletionRequestFunctionMessage.serializer(),
          value.value
        )
      is ChatCompletionRequestMessage.Third ->
        encoder.encodeSerializableValue(
          ChatCompletionRequestSystemMessage.serializer(),
          value.value
        )
      is ChatCompletionRequestMessage.Fourth ->
        encoder.encodeSerializableValue(ChatCompletionRequestToolMessage.serializer(), value.value)
      is ChatCompletionRequestMessage.Fifth ->
        encoder.encodeSerializableValue(ChatCompletionRequestUserMessage.serializer(), value.value)
    }
}
