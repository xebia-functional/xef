package com.xebia.functional.xef.reasoning.text.emotions

import kotlinx.serialization.Serializable

@Serializable
data class EmotionResult(
  val emotion: Emotion,
)
