package com.xebia.functional.openai.models.ext.completion.create

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CreateCompletionRequestModel(val value: String) {
  @SerialName(value = "babbage-002") `babbage_002`("babbage-002"),
  @SerialName(value = "davinci-002") `davinci_002`("davinci-002"),
  @SerialName(value = "gpt-3.5-turbo-instruct") `gpt_3_5_turbo_instruct`("gpt-3.5-turbo-instruct"),
  @SerialName(value = "text-davinci-003") `text_davinci_003`("text-davinci-003"),
  @SerialName(value = "text-davinci-002") `text_davinci_002`("text-davinci-002"),
  @SerialName(value = "text-davinci-001") `text_davinci_001`("text-davinci-001"),
  @SerialName(value = "code-davinci-002") `code_davinci_002`("code-davinci-002"),
  @SerialName(value = "text-curie-001") `text_curie_001`("text-curie-001"),
  @SerialName(value = "text-babbage-001") `text_babbage_001`("text-babbage-001"),
  @SerialName(value = "text-ada-001") `text_ada_001`("text-ada-001")
}
