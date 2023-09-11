package com.xebia.functional.xef.llm.models.usage

import kotlinx.serialization.Serializable

@Serializable
data class Usage(val promptTokens: Int?, val completionTokens: Int? = null, val totalTokens: Int?) {
  companion object {
    val ZERO: Usage = Usage(0, 0, 0)
  }
}
