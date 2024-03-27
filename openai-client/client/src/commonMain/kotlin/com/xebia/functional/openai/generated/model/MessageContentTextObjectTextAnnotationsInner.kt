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

@Serializable(with = MessageContentTextObjectTextAnnotationsInnerSerializer::class)
sealed interface MessageContentTextObjectTextAnnotationsInner {

  @JvmInline
  @Serializable
  value class CaseMessageContentTextAnnotationsFileCitationObject(
    val value: MessageContentTextAnnotationsFileCitationObject
  ) : MessageContentTextObjectTextAnnotationsInner

  @JvmInline
  @Serializable
  value class CaseMessageContentTextAnnotationsFilePathObject(
    val value: MessageContentTextAnnotationsFilePathObject
  ) : MessageContentTextObjectTextAnnotationsInner
}

private object MessageContentTextObjectTextAnnotationsInnerSerializer :
  KSerializer<MessageContentTextObjectTextAnnotationsInner> {
  @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
  override val descriptor: SerialDescriptor =
    buildSerialDescriptor("MessageContentTextObjectTextAnnotationsInner", PolymorphicKind.SEALED) {
      element("1", MessageContentTextAnnotationsFileCitationObject.serializer().descriptor)
      element("2", MessageContentTextAnnotationsFilePathObject.serializer().descriptor)
    }

  override fun deserialize(decoder: Decoder): MessageContentTextObjectTextAnnotationsInner {
    val json = decoder.decodeSerializableValue(JsonElement.serializer())
    return kotlin
      .runCatching {
        MessageContentTextObjectTextAnnotationsInner
          .CaseMessageContentTextAnnotationsFileCitationObject(
            Json.decodeFromJsonElement(
              MessageContentTextAnnotationsFileCitationObject.serializer(),
              json
            )
          )
      }
      .getOrNull()
      ?: kotlin
        .runCatching {
          MessageContentTextObjectTextAnnotationsInner
            .CaseMessageContentTextAnnotationsFilePathObject(
              Json.decodeFromJsonElement(
                MessageContentTextAnnotationsFilePathObject.serializer(),
                json
              )
            )
        }
        .getOrThrow()
  }

  override fun serialize(encoder: Encoder, value: MessageContentTextObjectTextAnnotationsInner) =
    when (value) {
      is MessageContentTextObjectTextAnnotationsInner.CaseMessageContentTextAnnotationsFileCitationObject ->
        encoder.encodeSerializableValue(
          MessageContentTextAnnotationsFileCitationObject.serializer(),
          value.value
        )
      is MessageContentTextObjectTextAnnotationsInner.CaseMessageContentTextAnnotationsFilePathObject ->
        encoder.encodeSerializableValue(
          MessageContentTextAnnotationsFilePathObject.serializer(),
          value.value
        )
    }
}
