package com.xebia.functional.xef.reasoning.code.performance

import kotlinx.serialization.Serializable

@Serializable
data class PerformanceOptimizationRecommendation(
  val category: String,
  val description: String,
  val examples: List<String>
)
