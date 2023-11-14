package com.xebia.functional.openai.models.ext.chat.create

import com.xebia.functional.tokenizer.ModelType
import kotlin.jvm.JvmInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateChatCompletionRequestModel {

  val value: String

  val modelType: ModelType
    get() =
      when (this) {
        is Custom -> TODO()
        is Standard ->
          when (this) {
            Standard.gpt_4_vision_preview -> ModelType.GPT_4_VISION_PREVIEW
            Standard.gpt_4_1106_preview -> ModelType.GPT_4_TURBO_1106_PREVIEW
            Standard.gpt_4 -> ModelType.GPT_4
            Standard.gpt_4_0314 -> ModelType.GPT_4_0314
            Standard.gpt_4_0613 -> ModelType.GPT_4_0613
            Standard.gpt_4_32k -> ModelType.GPT_4_32K
            Standard.gpt_4_32k_0314 -> ModelType.GPT_4_32_K_0314
            Standard.gpt_4_32k_0613 -> ModelType.GPT_4_32K_0613_FUNCTIONS
            Standard.gpt_3_5_turbo_1106 -> ModelType.GPT_3_5_TURBO_16_K
            Standard.gpt_3_5_turbo -> ModelType.GPT_3_5_TURBO
            Standard.gpt_3_5_turbo_16k -> ModelType.GPT_3_5_TURBO_16_K
            Standard.gpt_3_5_turbo_0301 -> ModelType.GPT_3_5_TURBO_0301
            Standard.gpt_3_5_turbo_0613 -> ModelType.GPT_3_5_TURBO_0613
            Standard.gpt_3_5_turbo_16k_0613 -> ModelType.GPT_3_5_TURBO_FUNCTIONS
          }
      }

  @JvmInline
  @Serializable
  value class Custom(override val value: String) : CreateChatCompletionRequestModel

  @Serializable
  enum class Standard(override val value: String) : CreateChatCompletionRequestModel {
    @SerialName(value = "gpt-4-vision-preview") `gpt_4_vision_preview`("gpt-4-vision-preview"),
    @SerialName(value = "gpt-4-1106-preview") `gpt_4_1106_preview`("gpt-4-1106-preview"),
    @SerialName(value = "gpt-4") `gpt_4`("gpt-4"),
    @SerialName(value = "gpt-4-0314") `gpt_4_0314`("gpt-4-0314"),
    @SerialName(value = "gpt-4-0613") `gpt_4_0613`("gpt-4-0613"),
    @SerialName(value = "gpt-4-32k") `gpt_4_32k`("gpt-4-32k"),
    @SerialName(value = "gpt-4-32k-0314") `gpt_4_32k_0314`("gpt-4-32k-0314"),
    @SerialName(value = "gpt-4-32k-0613") `gpt_4_32k_0613`("gpt-4-32k-0613"),
    @SerialName(value = "gpt-3.5-turbo-1106") `gpt_3_5_turbo_1106`("gpt-3.5-turbo-1106"),
    @SerialName(value = "gpt-3.5-turbo") `gpt_3_5_turbo`("gpt-3.5-turbo"),
    @SerialName(value = "gpt-3.5-turbo-16k") `gpt_3_5_turbo_16k`("gpt-3.5-turbo-16k"),
    @SerialName(value = "gpt-3.5-turbo-0301") `gpt_3_5_turbo_0301`("gpt-3.5-turbo-0301"),
    @SerialName(value = "gpt-3.5-turbo-0613") `gpt_3_5_turbo_0613`("gpt-3.5-turbo-0613"),
    @SerialName(value = "gpt-3.5-turbo-16k-0613") `gpt_3_5_turbo_16k_0613`("gpt-3.5-turbo-16k-0613")
  }
}
