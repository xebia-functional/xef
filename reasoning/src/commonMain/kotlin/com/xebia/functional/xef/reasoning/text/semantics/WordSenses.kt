package com.xebia.functional.xef.reasoning.text.semantics

import kotlinx.serialization.Serializable

@Serializable
data class WordSenses(
  val senses: List<WordSense>,
)
