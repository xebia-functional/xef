package com.xebia.functional.xef.reasoning.text.keywords

import kotlinx.serialization.Serializable

@Serializable
data class KeywordExtractionResult(
  val keywords: List<String>,
)
