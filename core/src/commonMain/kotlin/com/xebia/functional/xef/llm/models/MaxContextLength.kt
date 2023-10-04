package com.xebia.functional.xef.llm.models

sealed interface MaxContextLength {
  /** one total length of input and output combined */
  data class Combined(val total: Int) : MaxContextLength

  /** two separate max lengths for input and output respectively */
  data class FixIO(val input: Int, val output: Int) : MaxContextLength
}
