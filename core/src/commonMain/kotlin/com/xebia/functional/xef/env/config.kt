package com.xebia.functional.xef.env

import kotlin.jvm.JvmOverloads

data class OpenAIConfig
@JvmOverloads
constructor(
  val token: String =
    requireNotNull(getenv("OPENAI_TOKEN")) { "OpenAI Token missing from environment." },
  val baseUrl: String = "https://api.openai.com/v1/",
  val chunkSize: Int = 300,
  val requestTimeoutMillis: Long = 30_000
)

data class HuggingFaceConfig
@JvmOverloads
constructor(
  val token: String =
    requireNotNull(getenv("OPENAI_TOKEN")) { "OpenAI Token missing from environment." },
  val baseUrl: String = "https://api-inference.huggingface.co/"
)
