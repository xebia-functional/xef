package com.xebia.functional.xef.llm.models

/**
 * Describing the maximum context length a model
 * with text input and output might have.
 *
 * Some models from VertexAI (in 2023/10) have both types of max context length.
 */
sealed interface MaxIoContextLength {
  /** one total length of input and output combined */
  data class Combined(val total: Int) : MaxIoContextLength

  /** two separate max lengths for input and output respectively */
  data class Fix(val input: Int, val output: Int) : MaxIoContextLength
}
