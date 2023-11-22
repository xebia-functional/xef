package com.xebia.funcional.xef.evaluator.models

import kotlin.jvm.JvmSynthetic

data class OutputDescription(val value: String)

data class OutputResponse(val value: String) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(block: suspend () -> String): OutputResponse =
      OutputResponse(block())
  }
}

data class ContextDescription(val value: String)
