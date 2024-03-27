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

@Serializable(with = CreateChatCompletionRequestStopSerializer::class)
sealed interface CreateChatCompletionRequestStop {

  @JvmInline
  @Serializable
  value class CaseString(val value: kotlin.String) : CreateChatCompletionRequestStop

  @JvmInline
  @Serializable
  value class CaseStrings(val value: kotlin.collections.List<kotlin.String>) :
    CreateChatCompletionRequestStop
}

private object CreateChatCompletionRequestStopSerializer :
  KSerializer<CreateChatCompletionRequestStop> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("CreateChatCompletionRequestStop", PolymorphicKind.SEALED) {
      element("1", kotlin.String.serializer().descriptor)
      element("2", ListSerializer(kotlin.String.serializer()).descriptor)
    }

  override fun deserialize(decoder: Decoder): CreateChatCompletionRequestStop {
    val json = decoder.decodeSerializableValue(JsonElement.serializer())
    return kotlin
      .runCatching {
        CreateChatCompletionRequestStop.CaseString(
          Json.decodeFromJsonElement(kotlin.String.serializer(), json)
        )
      }
      .getOrNull()
      ?: kotlin
        .runCatching {
          CreateChatCompletionRequestStop.CaseStrings(
            Json.decodeFromJsonElement(ListSerializer(kotlin.String.serializer()), json)
          )
        }
        .getOrThrow()
  }

  override fun serialize(encoder: Encoder, value: CreateChatCompletionRequestStop) =
    when (value) {
      is CreateChatCompletionRequestStop.CaseString ->
        encoder.encodeSerializableValue(kotlin.String.serializer(), value.value)
      is CreateChatCompletionRequestStop.CaseStrings ->
        encoder.encodeSerializableValue(ListSerializer(kotlin.String.serializer()), value.value)
    }
}
