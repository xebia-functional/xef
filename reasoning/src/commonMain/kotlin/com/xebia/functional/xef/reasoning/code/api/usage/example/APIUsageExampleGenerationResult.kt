package com.xebia.functional.xef.reasoning.code.api.usage.example

import kotlinx.serialization.Serializable

@Serializable
data class APIUsageExampleGenerationResult(
  val examples: List<APIUsageExample>
)
