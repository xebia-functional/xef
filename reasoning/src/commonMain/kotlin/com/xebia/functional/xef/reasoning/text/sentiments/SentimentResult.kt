package com.xebia.functional.xef.reasoning.text.sentiments

import kotlinx.serialization.Serializable

@Serializable
data class SentimentResult(
  val sentiment: Sentiment,
  val score: Double,
)
