package com.xebia.functional.xef.reasoning.text.semantics

import com.xebia.functional.xef.auto.Description
import kotlinx.serialization.Serializable

@Serializable
data class TextualEntailmentResult(
  @Description(["The entailment relation between the premise and the hypothesis"])
  val entailment: Entailment,
)
