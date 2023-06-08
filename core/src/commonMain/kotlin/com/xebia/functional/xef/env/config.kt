package com.xebia.functional.xef.env

import arrow.resilience.Schedule
import kotlin.jvm.JvmOverloads
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class OpenAIConfig
@JvmOverloads
constructor(
  val token: String =
    requireNotNull(getenv("OPENAI_TOKEN")) { "OpenAI Token missing from environment." },
  val baseUrl: String = "https://api.openai.com/v1/",
  val chunkSize: Int = 300,
  val retryConfig: RetryConfig = RetryConfig(),
  val requestTimeoutMillis: Long = 30_000
)

data class RetryConfig(val backoff: Duration = 5.seconds, val maxRetries: Long = 5) {
  fun schedule(): Schedule<Throwable, Long> =
    Schedule.recurs<Throwable>(maxRetries)
      .zipLeft(Schedule.exponential<Throwable>(backoff).jittered(0.75, 1.25))
}

data class HuggingFaceConfig
@JvmOverloads
constructor(
  val token: String =
    requireNotNull(getenv("OPENAI_TOKEN")) { "OpenAI Token missing from environment." },
  val baseUrl: String = "https://api-inference.huggingface.co/"
)
