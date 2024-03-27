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

@Serializable(with = RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInnerSerializer::class)
sealed interface RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner {

  @JvmInline
  @Serializable
  value class First(val value: RunStepDetailsToolCallsCodeOutputImageObject) :
    RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner

  @JvmInline
  @Serializable
  value class Second(val value: RunStepDetailsToolCallsCodeOutputLogsObject) :
    RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner
}

private object RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInnerSerializer :
  KSerializer<RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor(
      "RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner",
      PolymorphicKind.SEALED
    ) {
      element("First", RunStepDetailsToolCallsCodeOutputImageObject.serializer().descriptor)
      element("Second", RunStepDetailsToolCallsCodeOutputLogsObject.serializer().descriptor)
    }

  override fun deserialize(
    decoder: Decoder
  ): RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner {
    val json = decoder.decodeSerializableValue(JsonElement.serializer())
    return kotlin
      .runCatching {
        RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner.First(
          Json.decodeFromJsonElement(
            RunStepDetailsToolCallsCodeOutputImageObject.serializer(),
            json
          )
        )
      }
      .getOrNull()
      ?: kotlin
        .runCatching {
          RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner.Second(
            Json.decodeFromJsonElement(
              RunStepDetailsToolCallsCodeOutputLogsObject.serializer(),
              json
            )
          )
        }
        .getOrThrow()
  }

  override fun serialize(
    encoder: Encoder,
    value: RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner
  ) =
    when (value) {
      is RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner.First ->
        encoder.encodeSerializableValue(
          RunStepDetailsToolCallsCodeOutputImageObject.serializer(),
          value.value
        )
      is RunStepDetailsToolCallsCodeObjectCodeInterpreterOutputsInner.Second ->
        encoder.encodeSerializableValue(
          RunStepDetailsToolCallsCodeOutputLogsObject.serializer(),
          value.value
        )
    }
}
