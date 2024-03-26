package com.xebia.functional.openai

import kotlinx.serialization.json.Json

data class Config(
  val baseUrl: String = "https://api.openai.com/v1",
  val token: String? = null,
  val org: String? = null,
  val json: Json = Json.Default,
  val streamingPrefix: String = "data:",
  val streamingDelimiter: String = "data: [DONE]"
)
