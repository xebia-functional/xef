package com.xebia.functional.openai.models.ext.finetune.job.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateFineTuningJobRequestHyperparametersNEpochs {
  @Serializable
  @JvmInline
  value class AutoValue(private val v: String = "auto") :
    CreateFineTuningJobRequestHyperparametersNEpochs {
    init {
      require(v == "auto") { "Only Auto is supported" }
    }
  }

  @Serializable
  @JvmInline
  value class IntValue(val v: Int) : CreateFineTuningJobRequestHyperparametersNEpochs {
    init {
      require(v in 1..50) { "Only values between 1 and 50 are allowed" }
    }
  }
}
