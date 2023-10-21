package com.xebia.functional.xef.conversation.llm.openai

import com.aallam.openai.api.core.Status
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.file.Purpose
import com.aallam.openai.api.file.fileUpload
import com.aallam.openai.api.finetuning.FineTuningJob
import com.aallam.openai.api.finetuning.Hyperparameters
import com.aallam.openai.api.finetuning.fineTuningRequest
import com.aallam.openai.api.model.ModelId
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import okio.FileSystem
import okio.Path.Companion.toPath

suspend fun fineTuneModel(token: String, suffix: String, baseModel: String, trainingFile: String, nEpochs: Int, fileSystem: FileSystem): String {
  val client = com.aallam.openai.client.OpenAI(token)

  val file =
    client.file(
      fileUpload {
        this.purpose = Purpose("fine-tune")
        this.file = FileSource(trainingFile.toPath(), fileSystem)
      }
    )
  val job =
    client.fineTuningJob(
      fineTuningRequest {
        this.model = ModelId(baseModel)
        this.trainingFile = file.id
        this.hyperparameters = Hyperparameters(nEpochs = nEpochs)
        this.suffix = suffix
      }
    )

  while (true) {
    delay(1.minutes)
    val latestJobUpdate = client.fineTuningJob(job.id) ?: error("job not found")
    if (latestJobUpdate.status in listOf(Status.Succeeded, Status.Failed)) break
  }

  val finishedJob = client.fineTuningJob(job.id) ?: error("job not found")
  if (finishedJob.status != Status.Succeeded) error("job didn't succeed")

  return finishedJob.id.id
}
