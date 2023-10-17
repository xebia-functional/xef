package com.xebia.functional.xef.conversation.finetuning

import arrow.core.getOrElse
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  val OAI = OpenAI()
  val baseModel = OAI.GPT_3_5_TURBO

  val fineTunedModelId = getenv("OPENAI_FINE_TUNED_MODEL_ID")
  val fineTuneJobId = getenv("OPENAI_FINE_TUNE_JOB_ID")

  val model = when {
    fineTunedModelId != null -> OAI.spawnModel(fineTunedModelId, baseModel)
    fineTuneJobId != null -> OAI.spawnFineTunedModel(fineTuneJobId, baseModel)
    else -> error("Please set the OPENAI_FINE_TUNED_MODEL_ID or OPENAI_FINE_TUNE_JOB_ID environment variable.")
  }.getOrElse { error(it) }

  OpenAI.conversation {
    while (true) {
      print("> ")
      val question = readlnOrNull() ?: break
      val answer = model.promptStreaming(Prompt(question), this)
      answer.collect(::print)
      println()
    }
  }
}
