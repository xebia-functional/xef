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
import kotlinx.serialization.json.*

@Serializable(with = ChatCompletionRequestMessageContentPartSerializer::class)
sealed interface ChatCompletionRequestMessageContentPart {

  @JvmInline
  @Serializable
  value class CaseChatCompletionRequestMessageContentPartImage(
    val value: ChatCompletionRequestMessageContentPartImage
  ) : ChatCompletionRequestMessageContentPart

  @JvmInline
  @Serializable
  value class CaseChatCompletionRequestMessageContentPartText(
    val value: ChatCompletionRequestMessageContentPartText
  ) : ChatCompletionRequestMessageContentPart
}

private object ChatCompletionRequestMessageContentPartSerializer :
  KSerializer<ChatCompletionRequestMessageContentPart> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("ChatCompletionRequestMessageContentPart", PolymorphicKind.SEALED) {
      element("1", ChatCompletionRequestMessageContentPartImage.serializer().descriptor)
      element("2", ChatCompletionRequestMessageContentPartText.serializer().descriptor)
    }

  override fun deserialize(decoder: Decoder): ChatCompletionRequestMessageContentPart {
    val json = decoder.decodeSerializableValue(JsonElement.serializer())
    return kotlin
      .runCatching {
        ChatCompletionRequestMessageContentPart.CaseChatCompletionRequestMessageContentPartImage(
          Json.decodeFromJsonElement(
            ChatCompletionRequestMessageContentPartImage.serializer(),
            json
          )
        )
      }
      .getOrNull()
      ?: kotlin
        .runCatching {
          ChatCompletionRequestMessageContentPart.CaseChatCompletionRequestMessageContentPartText(
            Json.decodeFromJsonElement(
              ChatCompletionRequestMessageContentPartText.serializer(),
              json
            )
          )
        }
        .getOrThrow()
  }

  override fun serialize(encoder: Encoder, value: ChatCompletionRequestMessageContentPart) =
    when (value) {
      is ChatCompletionRequestMessageContentPart.CaseChatCompletionRequestMessageContentPartImage ->
        encoder.encodeSerializableValue(
          ChatCompletionRequestMessageContentPartImage.serializer(),
          value.value
        )
      is ChatCompletionRequestMessageContentPart.CaseChatCompletionRequestMessageContentPartText ->
        encoder.encodeSerializableValue(
          ChatCompletionRequestMessageContentPartText.serializer(),
          value.value
        )
    }
}
