package com.xebia.functional.xef.reasoning.code.bug

import kotlinx.serialization.Serializable

@Serializable
data class BugDetectionResult(
  val bugs: List<Bug>,
)
