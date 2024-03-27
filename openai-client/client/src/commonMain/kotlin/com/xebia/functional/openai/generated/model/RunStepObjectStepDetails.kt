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

@Serializable(with = RunStepObjectStepDetailsSerializer::class)
sealed interface RunStepObjectStepDetails {

  @JvmInline
  @Serializable
  value class CaseRunStepDetailsMessageCreationObject(
    val value: RunStepDetailsMessageCreationObject
  ) : RunStepObjectStepDetails

  @JvmInline
  @Serializable
  value class CaseRunStepDetailsToolCallsObject(val value: RunStepDetailsToolCallsObject) :
    RunStepObjectStepDetails
}

private object RunStepObjectStepDetailsSerializer : KSerializer<RunStepObjectStepDetails> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("RunStepObjectStepDetails", PolymorphicKind.SEALED) {
      element("1", RunStepDetailsMessageCreationObject.serializer().descriptor)
      element("2", RunStepDetailsToolCallsObject.serializer().descriptor)
    }

  override fun deserialize(decoder: Decoder): RunStepObjectStepDetails {
    val json = decoder.decodeSerializableValue(JsonElement.serializer())
    return kotlin
      .runCatching {
        RunStepObjectStepDetails.CaseRunStepDetailsMessageCreationObject(
          Json.decodeFromJsonElement(RunStepDetailsMessageCreationObject.serializer(), json)
        )
      }
      .getOrNull()
      ?: kotlin
        .runCatching {
          RunStepObjectStepDetails.CaseRunStepDetailsToolCallsObject(
            Json.decodeFromJsonElement(RunStepDetailsToolCallsObject.serializer(), json)
          )
        }
        .getOrThrow()
  }

  override fun serialize(encoder: Encoder, value: RunStepObjectStepDetails) =
    when (value) {
      is RunStepObjectStepDetails.CaseRunStepDetailsMessageCreationObject ->
        encoder.encodeSerializableValue(
          RunStepDetailsMessageCreationObject.serializer(),
          value.value
        )
      is RunStepObjectStepDetails.CaseRunStepDetailsToolCallsObject ->
        encoder.encodeSerializableValue(RunStepDetailsToolCallsObject.serializer(), value.value)
    }
}
