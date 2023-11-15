package com.xebia.functional.xef.llm.models.usage

data class Usage(val promptTokens: Int?, val completionTokens: Int? = null, val totalTokens: Int?) {
  companion object {
    val ZERO: Usage = Usage(0, 0, 0)
  }
}
