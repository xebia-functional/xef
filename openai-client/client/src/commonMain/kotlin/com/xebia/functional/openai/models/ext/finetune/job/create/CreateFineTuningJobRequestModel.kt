package com.xebia.functional.openai.models.ext.finetune.job.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateFineTuningJobRequestModel(val value: String) {
  @SerialName(value = "babbage-002") `babbage_002`("babbage-002"),
  @SerialName(value = "davinci-002") `davinci_002`("davinci-002"),
  @SerialName(value = "gpt-3.5-turbo") `gpt_3_5_turbo`("gpt-3.5-turbo")
}
