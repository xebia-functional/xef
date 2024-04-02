package com.xebia.functional.openai

import kotlinx.serialization.json.Json

data class Config(
  val baseUrl: String,
  val token: String,
  val org: String?,
  val json: Json,
  val streamingPrefix: String,
  val streamingDelimiter: String
)
