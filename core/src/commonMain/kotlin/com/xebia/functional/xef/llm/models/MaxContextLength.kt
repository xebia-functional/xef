package com.xebia.functional.xef.llm.models

sealed interface MaxContextLength {
  data class Combined(val total: Int) : MaxContextLength

  data class IO(val input: Int, val output: Int) : MaxContextLength
}
