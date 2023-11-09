package com.xebia.functional.openai.models.ext.finetune.job.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateFineTuningJobRequestHyperparametersLearningRateMultiplier {
  @Serializable
  @JvmInline
  value class AutoValue(private val v: String = "auto") :
    CreateFineTuningJobRequestHyperparametersLearningRateMultiplier {
    init {
      require(v == "auto") { "Only Auto is supported" }
    }
  }

  @Serializable
  @JvmInline
  value class DoubleValue(val v: Double) :
    CreateFineTuningJobRequestHyperparametersLearningRateMultiplier {
    init {
      require(v > 0) { "Only values greater than 0 are allowed" }
    }
  }
}
