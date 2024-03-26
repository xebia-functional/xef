/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import com.xebia.functional.openai.generated.model.CreateFineTuningJobRequestModel.Supported.babbage_002
import com.xebia.functional.openai.generated.model.CreateFineTuningJobRequestModel.Supported.davinci_002
import com.xebia.functional.openai.generated.model.CreateFineTuningJobRequestModel.Supported.gpt_3_5_turbo
import kotlin.jvm.JvmStatic
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.encoding.*

/**
 * The name of the model to fine-tune. You can select one of the
 * [supported models](/docs/guides/fine-tuning/what-models-can-be-fine-tuned).
 */
// We define a serializer for the parent sum type, and then use it to serialize the child types
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = CreateFineTuningJobRequestModelSerializer::class)
sealed interface CreateFineTuningJobRequestModel {
  val value: kotlin.String

  @Serializable(with = CreateFineTuningJobRequestModelSerializer::class)
  enum class Supported(override val value: kotlin.String) : CreateFineTuningJobRequestModel {
    @SerialName(value = "babbage-002") babbage_002("babbage-002"),
    @SerialName(value = "davinci-002") davinci_002("davinci-002"),
    @SerialName(value = "gpt-3.5-turbo") gpt_3_5_turbo("gpt-3.5-turbo");

    override fun toString(): kotlin.String = value
  }

  @Serializable(with = CreateFineTuningJobRequestModelSerializer::class)
  data class Custom(override val value: kotlin.String) : CreateFineTuningJobRequestModel

  companion object {
    @JvmStatic
    fun valueOf(value: kotlin.String): CreateFineTuningJobRequestModel =
      values().firstOrNull { it.value == value } ?: Custom(value)

    inline val babbage_002: CreateFineTuningJobRequestModel
      get() = Supported.babbage_002

    inline val davinci_002: CreateFineTuningJobRequestModel
      get() = Supported.davinci_002

    inline val gpt_3_5_turbo: CreateFineTuningJobRequestModel
      get() = Supported.gpt_3_5_turbo

    @JvmStatic fun values(): List<CreateFineTuningJobRequestModel> = Supported.entries
  }
}

private object CreateFineTuningJobRequestModelSerializer :
  KSerializer<CreateFineTuningJobRequestModel> {
  private val valueSerializer = kotlin.String.serializer()
  override val descriptor = valueSerializer.descriptor

  override fun deserialize(decoder: Decoder): CreateFineTuningJobRequestModel {
    val value = decoder.decodeSerializableValue(valueSerializer)
    return CreateFineTuningJobRequestModel.valueOf(value)
  }

  override fun serialize(encoder: Encoder, value: CreateFineTuningJobRequestModel) {
    encoder.encodeSerializableValue(valueSerializer, value.value)
  }
}
