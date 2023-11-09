package com.xebia.functional.openai.models.ext.finetune.job

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface FineTuningJobRequestHyperparametersNEpochs {
  @Serializable
  @JvmInline
  value class AutoValue(private val v: String = "auto") :
    FineTuningJobRequestHyperparametersNEpochs {
    init {
      require(v == "auto") { "Only Auto is supported" }
    }
  }

  @Serializable
  @JvmInline
  value class IntValue(val v: Int) : FineTuningJobRequestHyperparametersNEpochs {
    init {
      require(v in 1..50) { "Only values between 1 and 50 are allowed" }
    }
  }
}
