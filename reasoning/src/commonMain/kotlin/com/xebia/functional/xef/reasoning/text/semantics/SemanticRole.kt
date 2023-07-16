package com.xebia.functional.xef.reasoning.text.semantics

import kotlinx.serialization.Serializable

@Serializable
data class SemanticRole(
  val word: String,
  val role: String,
)
