/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import com.xebia.functional.openai.generated.model.CreateModerationRequestModel.Supported.text_moderation_latest
import com.xebia.functional.openai.generated.model.CreateModerationRequestModel.Supported.text_moderation_stable
import kotlin.jvm.JvmStatic
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.encoding.*

/**
 * Two content moderations models are available: `text-moderation-stable` and
 * `text-moderation-latest`. The default is `text-moderation-latest` which will be automatically
 * upgraded over time. This ensures you are always using our most accurate model. If you use
 * `text-moderation-stable`, we will provide advanced notice before updating the model. Accuracy of
 * `text-moderation-stable` may be slightly lower than for `text-moderation-latest`.
 *
 * Values: latest,stable
 */
// We define a serializer for the parent sum type,
// and then use it to serialize the child types
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = CreateModerationRequestModelSerializer::class)
sealed interface CreateModerationRequestModel {
  val name: kotlin.String

  @Serializable(with = CreateModerationRequestModelSerializer::class)
  enum class Supported(name: kotlin.String) : CreateModerationRequestModel {
    @SerialName(value = "text-moderation-latest") text_moderation_latest("text-moderation-latest"),
    @SerialName(value = "text-moderation-stable") text_moderation_stable("text-moderation-stable");

    override fun toString(): kotlin.String = name
  }

  @Serializable(with = CreateModerationRequestModelSerializer::class)
  data class Custom(override val name: kotlin.String) : CreateModerationRequestModel

  companion object {
    @JvmStatic
    fun valueOf(name: kotlin.String): CreateModerationRequestModel =
      values().firstOrNull { it.name == name } ?: Custom(name)

    inline val text_moderation_latest: CreateModerationRequestModel
      get() = Supported.text_moderation_latest

    inline val text_moderation_stable: CreateModerationRequestModel
      get() = Supported.text_moderation_stable

    @JvmStatic fun values(): List<CreateModerationRequestModel> = Supported.entries

    // Is this resulting in a recursive loop!?
    //      @JvmStatic
    //      fun serializer(): KSerializer<CreateModerationRequestModel> =
    //        CreateModerationRequestModelSerializer
  }
}

private object CreateModerationRequestModelSerializer : KSerializer<CreateModerationRequestModel> {
  private val valueSerializer = kotlin.String.serializer()
  override val descriptor = valueSerializer.descriptor

  override fun deserialize(decoder: Decoder): CreateModerationRequestModel {
    val value = decoder.decodeSerializableValue(valueSerializer)
    return CreateModerationRequestModel.valueOf(value)
  }

  override fun serialize(encoder: Encoder, value: CreateModerationRequestModel) {
    encoder.encodeSerializableValue(valueSerializer, value.name)
  }
}
