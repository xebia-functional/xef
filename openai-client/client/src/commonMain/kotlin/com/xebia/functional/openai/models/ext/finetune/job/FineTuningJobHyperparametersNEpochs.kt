package com.xebia.functional.openai.models.ext.finetune.job

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface FineTuningJobHyperparametersNEpochs {
  @Serializable
  @JvmInline
  value class AutoValue(private val v: String = "auto") : FineTuningJobHyperparametersNEpochs {
    init {
      require(v == "auto") { "Only Auto is supported" }
    }
  }

  @Serializable
  @JvmInline
  value class IntValue(val v: Int) : FineTuningJobHyperparametersNEpochs {
    init {
      require(v in 1..50) { "Only values between 1 and 50 are allowed" }
    }
  }
}
