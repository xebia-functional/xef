package com.xebia.functional.openai.models.ext.chat.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateChatCompletionRequestModel(val value: String) {
  @SerialName(value = "gpt-4") `gpt_4`("gpt-4"),
  @SerialName(value = "gpt-4-0314") `gpt_4_0314`("gpt-4-0314"),
  @SerialName(value = "gpt-4-0613") `gpt_4_0613`("gpt-4-0613"),
  @SerialName(value = "gpt-4-32k") `gpt_4_32k`("gpt-4-32k"),
  @SerialName(value = "gpt-4-32k-0314") `gpt_4_32k_0314`("gpt-4-32k-0314"),
  @SerialName(value = "gpt-4-32k-0613") `gpt_4_32k_0613`("gpt-4-32k-0613"),
  @SerialName(value = "gpt-3.5-turbo") `gpt_3_5_turbo`("gpt-3.5-turbo"),
  @SerialName(value = "gpt-3.5-turbo-16k") `gpt_3_5_turbo_16k`("gpt-3.5-turbo-16k"),
  @SerialName(value = "gpt-3.5-turbo-0301") `gpt_3_5_turbo_0301`("gpt-3.5-turbo-0301"),
  @SerialName(value = "gpt-3.5-turbo-0613") `gpt_3_5_turbo_0613`("gpt-3.5-turbo-0613"),
  @SerialName(value = "gpt-3.5-turbo-16k-0613") `gpt_3_5_turbo_16k_0613`("gpt-3.5-turbo-16k-0613")
}
