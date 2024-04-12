package com.xebia.functional.openai

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ServerSentEvent(
  val event: String? = null,
  val data: JsonElement? = null,
)
