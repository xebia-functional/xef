package com.xebia.functional.xef.reasoning.code.performance

import kotlinx.serialization.Serializable

@Serializable
data class PerformanceOptimizationResult(
  val recommendations: List<PerformanceOptimizationRecommendation>
)
