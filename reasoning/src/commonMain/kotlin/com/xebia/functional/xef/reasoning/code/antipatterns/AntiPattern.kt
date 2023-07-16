package com.xebia.functional.xef.reasoning.code.antipatterns

import kotlinx.serialization.Serializable

@Serializable
data class AntiPattern(
  val name: String,
  val description: String,
  val examples: List<String>
)
