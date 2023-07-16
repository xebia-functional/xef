package com.xebia.functional.xef.reasoning.code.antipatterns

import kotlinx.serialization.Serializable

@Serializable
data class AntiPatternDetectionResult(
  val detectedAntiPatterns: List<AntiPattern>,
)
