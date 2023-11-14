package ai.xef.openai

import com.xebia.functional.tokenizer.ModelType
import kotlin.jvm.JvmInline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = OpenAIModelSerializer::class)
sealed interface OpenAIModel<T> {

  fun value(): String = TODO()

  fun modelType(): ModelType = TODO()
  //    get() =
  //      when (this) {
  //        is CreateChatCompletionRequestModel.Custom -> TODO()
  //        is CreateChatCompletionRequestModel.Standard ->
  //          when (this) {
  //            CreateChatCompletionRequestModel.Standard.gpt_4_vision_preview ->
  // ModelType.GPT_4_VISION_PREVIEW
  //            CreateChatCompletionRequestModel.Standard.gpt_4_1106_preview ->
  // ModelType.GPT_4_TURBO_1106_PREVIEW
  //            CreateChatCompletionRequestModel.Standard.gpt_4 -> ModelType.GPT_4
  //            CreateChatCompletionRequestModel.Standard.gpt_4_0314 -> ModelType.GPT_4_0314
  //            CreateChatCompletionRequestModel.Standard.gpt_4_0613 -> ModelType.GPT_4_0613
  //            CreateChatCompletionRequestModel.Standard.gpt_4_32k -> ModelType.GPT_4_32K
  //            CreateChatCompletionRequestModel.Standard.gpt_4_32k_0314 ->
  // ModelType.GPT_4_32_K_0314
  //            CreateChatCompletionRequestModel.Standard.gpt_4_32k_0613 ->
  // ModelType.GPT_4_32K_0613_FUNCTIONS
  //            CreateChatCompletionRequestModel.Standard.gpt_3_5_turbo_1106 ->
  // ModelType.GPT_3_5_TURBO_16_K
  //            CreateChatCompletionRequestModel.Standard.gpt_3_5_turbo -> ModelType.GPT_3_5_TURBO
  //            CreateChatCompletionRequestModel.Standard.gpt_3_5_turbo_16k ->
  // ModelType.GPT_3_5_TURBO_16_K
  //            CreateChatCompletionRequestModel.Standard.gpt_3_5_turbo_0301 ->
  // ModelType.GPT_3_5_TURBO_0301
  //            CreateChatCompletionRequestModel.Standard.gpt_3_5_turbo_0613 ->
  // ModelType.GPT_3_5_TURBO_0613
  //            CreateChatCompletionRequestModel.Standard.gpt_3_5_turbo_16k_0613 ->
  // ModelType.GPT_3_5_TURBO_FUNCTIONS
  //          }
  //      }
}

@Serializable @JvmInline value class CustomModel<T>(val model: String) : OpenAIModel<T>

@Serializable @JvmInline value class StandardModel<T>(val model: T) : OpenAIModel<T>

class OpenAIModelSerializer<T>(private val dataSerializer: KSerializer<T>) :
  KSerializer<OpenAIModel<T>> {
  override val descriptor: SerialDescriptor = OpenAIModel.serializer(dataSerializer).descriptor

  override fun serialize(encoder: Encoder, value: OpenAIModel<T>) =
    when (value) {
      is CustomModel<T> -> String.serializer().serialize(encoder, value.model)
      is StandardModel<T> -> dataSerializer.serialize(encoder, value.model)
      else -> {}
    }

  override fun deserialize(decoder: Decoder) =
    try {
      StandardModel(dataSerializer.deserialize(decoder))
    } catch (e: SerializationException) {
      CustomModel(String.serializer().deserialize(decoder))
    } catch (e: IllegalArgumentException) {
      CustomModel(String.serializer().deserialize(decoder))
    }
}
