package com.xebia.functional.env

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.recover
import arrow.core.raise.zipOrAccumulate
import io.ktor.http.Url as KUrl
import arrow.resilience.Schedule
import com.xebia.functional.AIError
import com.xebia.functional.auto.AI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Env(val openAI: OpenAIConfig, val huggingFace: HuggingFaceConfig)

data class OpenAIConfig(val token: String, val baseUrl: KUrl, val chunkSize: Int, val retryConfig: RetryConfig)

data class RetryConfig(val backoff: Duration, val maxRetries: Long) {
  fun schedule(): Schedule<Throwable, Long> =
    Schedule.recurs<Throwable>(maxRetries)
      .zipLeft(Schedule.exponential<Throwable>(backoff).jittered(0.75, 1.25))
}

data class HuggingFaceConfig(val token: String, val baseUrl: KUrl)

fun Raise<NonEmptyList<AIError.Env>>.Env(): Env =
  zipOrAccumulate(
    { OpenAIConfig() },
    { withNel { HuggingFaceConfig() } }
  ) { openAI, huggingFace -> Env(openAI, huggingFace) }

fun Raise<AIError.Env.OpenAI>.OpenAIConfig(token: String? = null) =
  recover({
    zipOrAccumulate(
      { token ?: env("OPENAI_TOKEN") },
      { env("OPENAI_BASE_URI", default = Url("https://api.openai.com/v1/")) { Url(it) } },
      { env("OPENAI_CHUNK_SIZE", default = 300) { it.toIntOrNull() } },
      { env("OPENAI_BACKOFF", default = 5.seconds) { it.toIntOrNull()?.seconds } },
      { env("OPENAI_MAX_RETRIES", default = 5) { it.toLongOrNull() } },
    ) { token2, baseUrl, chunkSize, backoff, maxRetries ->
      OpenAIConfig(
        token2,
        baseUrl,
        chunkSize,
        RetryConfig(backoff, maxRetries)
      )
    }
  }) { e: NonEmptyList<String> -> raise(AIError.Env.OpenAI(e)) }

fun Raise<AIError.Env.HuggingFace>.HuggingFaceConfig(token: String? = null) =
  recover({
    zipOrAccumulate(
      { token ?: env("HF_TOKEN") },
      { env("HF_BASE_URI", default = Url("https://api-inference.huggingface.co/")) { Url(it) } }
    ) { token2, baseUrl -> HuggingFaceConfig(token2, baseUrl) }
  }) { e: NonEmptyList<String> -> raise(AIError.Env.HuggingFace(e)) }

fun Raise<String>.Url(urlString: String): KUrl =
  catch({ KUrl(urlString) }) { raise(it.message ?: "Invalid url: $it") }
