package com.xebia.functional.xef.evaluator.models

import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.Serializable

@Serializable data class OutputDescription(val value: String)

@Serializable
data class OutputResponse(val description: OutputDescription, val value: String) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      description: OutputDescription,
      block: suspend () -> String
    ): OutputResponse = OutputResponse(description, block())
  }
}
