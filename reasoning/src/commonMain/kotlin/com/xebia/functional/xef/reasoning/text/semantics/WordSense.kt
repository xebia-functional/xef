package com.xebia.functional.xef.reasoning.text.semantics

import kotlinx.serialization.Serializable

@Serializable
data class WordSense(
  val word: String,
  val sense: String,
)
