package com.xebia.functional.xef.reasoning.text.arguments

import kotlinx.serialization.Serializable

@Serializable
data class StanceDetectionResult(
  val stance: Stance
)
