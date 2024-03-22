package com.xebia.functional.xef.conversation.finetuning

import ai.xef.openai.CustomModel
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {

  val fineTunedModelId = getenv("OPENAI_FINE_TUNED_MODEL_ID")
  val fineTuneJobId = getenv("OPENAI_FINE_TUNE_JOB_ID")

  val model: CustomModel<CreateChatCompletionRequestModel> =
    when {
      fineTunedModelId != null -> CustomModel(fineTunedModelId)
      fineTuneJobId != null -> CustomModel(fineTuneJobId)
      else ->
        error(
          "Please set the OPENAI_FINE_TUNED_MODEL_ID or OPENAI_FINE_TUNE_JOB_ID environment variable."
        )
    }

  Conversation {
    while (true) {
      print("> ")
      val question = readlnOrNull() ?: break
      val answer = promptStreaming(Prompt(model, question))
      answer.collect(::print)
      println()
    }
  }
}
