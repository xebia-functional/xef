package com.xebia.functional.xef.reasoning.text.language

import kotlinx.serialization.Serializable

@Serializable
data class LanguageIdentificationResult(
  val language: String
)
