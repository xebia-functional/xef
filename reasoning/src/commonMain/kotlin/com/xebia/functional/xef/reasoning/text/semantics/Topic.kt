package com.xebia.functional.xef.reasoning.text.semantics

import kotlinx.serialization.Serializable

@Serializable
data class Topic(
  val name: String,
  val relevance: Double,
)
