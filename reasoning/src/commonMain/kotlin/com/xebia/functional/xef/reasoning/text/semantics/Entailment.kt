package com.xebia.functional.xef.reasoning.text.semantics

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
enum class Entailment {
  @Description(["The hypothesis is logically implied by the premise"])
  ENTAILMENT,
  @Description(["The hypothesis is logically contradicted by the premise"])
  CONTRADICTION,
  @Description(["Neither entailment nor contradiction can be determined"])
  NEUTRAL
}
