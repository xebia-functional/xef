package com.xebia.functional.openai.models.ext.finetune.job.create

import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

@Serializable
sealed interface CreateFineTuningJobRequestHyperparametersBatchSize {
  @Serializable
  @JvmInline
  value class AutoValue(private val v: String = "auto") :
    CreateFineTuningJobRequestHyperparametersBatchSize {
    init {
      require(v == "auto") { "Only Auto is supported" }
    }
  }

  @Serializable
  @JvmInline
  value class IntValue(val v: Int) : CreateFineTuningJobRequestHyperparametersBatchSize {
    init {
      require(v in 1..256) { "Only values between 1 and 256 are allowed" }
    }
  }
}
