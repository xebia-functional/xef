package com.xebia.functional.xef.reasoning.text.grammar

import kotlinx.serialization.Serializable

@Serializable
data class GrammarCorrectionResult(
  val correctedText: String,
)
