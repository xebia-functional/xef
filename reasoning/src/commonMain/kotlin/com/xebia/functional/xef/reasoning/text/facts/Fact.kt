package com.xebia.functional.xef.reasoning.text.facts

import kotlinx.serialization.Serializable

@Serializable
enum class Fact {
  TRUE,
  FALSE,
  UNVERIFIABLE
}
