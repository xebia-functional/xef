package com.xebia.functional.xef.conversation.streaming

import ai.xef.openai.StandardModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.llm.StreamedFunction
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.Serializable

@Serializable
data class Synopsis(
  @Description("The synopsis of the argument in 100 words") val synopsis: String,
  @Description("The score of the argument") val score: Double
)

@Serializable
data class MeaningOfLifeArgument(
  @Description("The author of the argument") val author: String,
  @Description("The argument or stand the author takes") val argument: String,
  @Description("A detailed synopsys of the argument") val synopsis: Synopsis
)

suspend fun main() {
  Conversation {
    val model = StandardModel(CreateChatCompletionRequestModel.gpt_3_5_turbo_16k_0613)
    promptStreamingFunctions<MeaningOfLifeArgument>(
        Prompt(model, "Provide arguments and authors for the meaning of life")
      )
      .collect { element ->
        when (element) {
          is StreamedFunction.Property -> {
            println("${element.path} = ${element.value}")
          }
          is StreamedFunction.Result -> {
            println(element.value)
          }
        }
      }
  }
}
