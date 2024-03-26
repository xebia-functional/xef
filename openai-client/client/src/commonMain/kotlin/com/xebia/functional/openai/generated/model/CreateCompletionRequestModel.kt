/**
 * Please note: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 */
@file:Suppress("ArrayInDataClass", "EnumEntryName", "RemoveRedundantQualifierName", "UnusedImport")

package com.xebia.functional.openai.generated.model

import com.xebia.functional.openai.generated.model.CreateCompletionRequestModel.Supported.babbage_002
import com.xebia.functional.openai.generated.model.CreateCompletionRequestModel.Supported.davinci_002
import com.xebia.functional.openai.generated.model.CreateCompletionRequestModel.Supported.gpt_3_5_turbo_instruct
import kotlin.jvm.JvmStatic
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.encoding.*

/**
 * ID of the model to use. You can use the [List models](/docs/api-reference/models/list) API to see
 * all of your available models, or see our [Model overview](/docs/models/overview) for descriptions
 * of them.
 */
// We define a serializer for the parent sum type, and then use it to serialize the child types
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
@Serializable(with = CreateCompletionRequestModelSerializer::class)
sealed interface CreateCompletionRequestModel {
  val value: kotlin.String

  @Serializable(with = CreateCompletionRequestModelSerializer::class)
  enum class Supported(override val value: kotlin.String) : CreateCompletionRequestModel {
    @SerialName(value = "gpt-3.5-turbo-instruct") gpt_3_5_turbo_instruct("gpt-3.5-turbo-instruct"),
    @SerialName(value = "davinci-002") davinci_002("davinci-002"),
    @SerialName(value = "babbage-002") babbage_002("babbage-002");

    override fun toString(): kotlin.String = value
  }

  @Serializable(with = CreateCompletionRequestModelSerializer::class)
  data class Custom(override val value: kotlin.String) : CreateCompletionRequestModel

  companion object {
    @JvmStatic
    fun valueOf(value: kotlin.String): CreateCompletionRequestModel =
      values().firstOrNull { it.value == value } ?: Custom(value)

    inline val gpt_3_5_turbo_instruct: CreateCompletionRequestModel
      get() = Supported.gpt_3_5_turbo_instruct

    inline val davinci_002: CreateCompletionRequestModel
      get() = Supported.davinci_002

    inline val babbage_002: CreateCompletionRequestModel
      get() = Supported.babbage_002

    @JvmStatic fun values(): List<CreateCompletionRequestModel> = Supported.entries
  }
}

private object CreateCompletionRequestModelSerializer : KSerializer<CreateCompletionRequestModel> {
  private val valueSerializer = kotlin.String.serializer()
  override val descriptor = valueSerializer.descriptor

  override fun deserialize(decoder: Decoder): CreateCompletionRequestModel {
    val value = decoder.decodeSerializableValue(valueSerializer)
    return CreateCompletionRequestModel.valueOf(value)
  }

  override fun serialize(encoder: Encoder, value: CreateCompletionRequestModel) {
    encoder.encodeSerializableValue(valueSerializer, value.value)
  }
}
