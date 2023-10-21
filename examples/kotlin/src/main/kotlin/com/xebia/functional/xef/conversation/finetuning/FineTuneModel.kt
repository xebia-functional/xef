package com.xebia.functional.xef.conversation.finetuning

import com.xebia.functional.xef.conversation.llm.openai.fineTuneModel
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.io.DEFAULT
import okio.FileSystem

suspend fun main() {
  val jobId = fineTuneModel(
    token = getenv("OPENAI_TOKEN")!!,
    suffix = "v0",
    baseModel = "gpt-3.5-turbo",
    trainingFile = "fine-tuning-samples-ron.jsonl",
    nEpochs = 5,
    FileSystem.DEFAULT,
  )
  println("fine tune job: $jobId")
}
