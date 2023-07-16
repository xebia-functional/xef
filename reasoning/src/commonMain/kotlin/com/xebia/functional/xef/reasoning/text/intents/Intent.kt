package com.xebia.functional.xef.reasoning.text.intents

import kotlinx.serialization.Serializable

@Serializable
enum class Intent {
  GREETING,
  ORDER,
  PAYMENT,
  SUPPORT,
  OTHER
}
