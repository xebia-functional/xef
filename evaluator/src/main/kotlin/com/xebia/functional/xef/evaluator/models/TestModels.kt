package com.xebia.functional.xef.evaluator.models

import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable

data class OutputDescription(val value: String)

data class OutputResponse(val value: String) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(block: suspend () -> String): OutputResponse =
      OutputResponse(block())
  }
}

@Serializable data class ContextDescription(val value: String)
