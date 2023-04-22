package com.xebia.functional.env

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.recover
import arrow.core.raise.zipOrAccumulate
import io.ktor.http.Url as KUrl
import arrow.resilience.Schedule
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class InvalidConfig(val message: String)

data class Env(val openAI: OpenAIConfig, val huggingFace: HuggingFaceConfig)

data class OpenAIConfig(val token: String, val chunkSize: Int, val retryConfig: RetryConfig)

data class RetryConfig(val backoff: Duration, val maxRetries: Long) {
  fun schedule(): Schedule<Throwable, Unit> =
    Schedule.recurs<Throwable>(maxRetries)
      .and(Schedule.exponential(backoff))
      .jittered(0.75, 1.25)
      .map { }
}

data class HuggingFaceConfig(val token: String, val baseUrl: KUrl)

fun Raise<InvalidConfig>.Env(): Env =
  recover({
    zipOrAccumulate(
      { OpenAIConfig() },
      { HuggingFaceConfig() }
    ) { openAI, huggingFace -> Env(openAI, huggingFace) }
  }) { nel -> raise(InvalidConfig(nel.joinToString(separator = "\n"))) }

fun Raise<NonEmptyList<String>>.OpenAIConfig(token: String? = null) =
  zipOrAccumulate(
    { token ?: env("OPENAI_TOKEN") },
    { env("OPENAI_CHUNK_SIZE", default = 1000) { it.toIntOrNull() } },
    { env("OPENAI_BACKOFF", default = 5.seconds) { it.toIntOrNull()?.seconds } },
    { env("OPENAI_MAX_RETRIES", default = 5) { it.toLongOrNull() } },
  ) { token2, chunkSize, backoff, maxRetries -> OpenAIConfig(token2, chunkSize, RetryConfig(backoff, maxRetries)) }

fun Raise<NonEmptyList<String>>.HuggingFaceConfig(token: String? = null) =
  zipOrAccumulate(
    { token ?: env("HF_TOKEN") },
    { env("HF_BASE_URI", default = Url("https://api-inference.huggingface.co")) { Url(it) } }
  ) { token2, baseUrl -> HuggingFaceConfig(token2, baseUrl) }

fun Raise<String>.Url(urlString: String): KUrl =
  catch({ KUrl(urlString) }) { raise(it.message ?: "Invalid url: $it") }
