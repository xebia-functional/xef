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

@Serializable(with = CreateCompletionRequestPromptSerializer::class)
sealed interface CreateCompletionRequestPrompt {

  @JvmInline
  @Serializable
  value class CaseString(val value: kotlin.String) : CreateCompletionRequestPrompt

  @JvmInline
  @Serializable
  value class CaseInts(val value: kotlin.collections.List<kotlin.Int>) :
    CreateCompletionRequestPrompt

  @JvmInline
  @Serializable
  value class CaseStrings(val value: kotlin.collections.List<kotlin.String>) :
    CreateCompletionRequestPrompt

  @JvmInline
  @Serializable
  value class CaseIntsList(
    val value: kotlin.collections.List<kotlin.collections.List<kotlin.Int>>
  ) : CreateCompletionRequestPrompt
}

private object CreateCompletionRequestPromptSerializer :
  KSerializer<CreateCompletionRequestPrompt> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("CreateCompletionRequestPrompt", PolymorphicKind.SEALED) {
      element("1", kotlin.String.serializer().descriptor)
      element("2", ListSerializer(kotlin.Int.serializer()).descriptor)
      element("3", ListSerializer(kotlin.String.serializer()).descriptor)
      element("4", ListSerializer(ListSerializer(kotlin.Int.serializer())).descriptor)
    }

  override fun deserialize(decoder: Decoder): CreateCompletionRequestPrompt {
    val json = decoder.decodeSerializableValue(JsonElement.serializer())
    return kotlin
      .runCatching {
        CreateCompletionRequestPrompt.CaseString(
          Json.decodeFromJsonElement(kotlin.String.serializer(), json)
        )
      }
      .getOrNull()
      ?: kotlin
        .runCatching {
          CreateCompletionRequestPrompt.CaseInts(
            Json.decodeFromJsonElement(ListSerializer(kotlin.Int.serializer()), json)
          )
        }
        .getOrNull()
      ?: kotlin
        .runCatching {
          CreateCompletionRequestPrompt.CaseStrings(
            Json.decodeFromJsonElement(ListSerializer(kotlin.String.serializer()), json)
          )
        }
        .getOrNull()
      ?: kotlin
        .runCatching {
          CreateCompletionRequestPrompt.CaseIntsList(
            Json.decodeFromJsonElement(
              ListSerializer(ListSerializer(kotlin.Int.serializer())),
              json
            )
          )
        }
        .getOrThrow()
  }

  override fun serialize(encoder: Encoder, value: CreateCompletionRequestPrompt) =
    when (value) {
      is CreateCompletionRequestPrompt.CaseString ->
        encoder.encodeSerializableValue(kotlin.String.serializer(), value.value)
      is CreateCompletionRequestPrompt.CaseInts ->
        encoder.encodeSerializableValue(ListSerializer(kotlin.Int.serializer()), value.value)
      is CreateCompletionRequestPrompt.CaseStrings ->
        encoder.encodeSerializableValue(ListSerializer(kotlin.String.serializer()), value.value)
      is CreateCompletionRequestPrompt.CaseIntsList ->
        encoder.encodeSerializableValue(
          ListSerializer(ListSerializer(kotlin.Int.serializer())),
          value.value
        )
    }
}
