package com.xebia.functional.xef.env

import arrow.core.NonEmptyList
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.recover
import arrow.core.raise.zipOrAccumulate
import com.xebia.functional.xef.AIError
import io.ktor.http.Url as KUrl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class Env(val openAI: OpenAIConfig, val huggingFace: HuggingFaceConfig)

data class OpenAIConfig(
  val token: String,
  val baseUrl: KUrl,
  val chunkSize: Int,
  val requestTimeout: Duration
)

data class HuggingFaceConfig(val token: String, val baseUrl: KUrl)

fun Raise<NonEmptyList<AIError.Env>>.Env(): Env =
  zipOrAccumulate({ OpenAIConfig() }, { withNel { HuggingFaceConfig() } }) { openAI, huggingFace ->
    Env(openAI, huggingFace)
  }

fun Raise<AIError.Env.OpenAI>.OpenAIConfig(token: String? = null) =
  recover({
    zipOrAccumulate(
      { token ?: env("OPENAI_TOKEN") },
      { env("OPENAI_BASE_URI", default = Url("https://api.openai.com/v1/")) { Url(it) } },
      { env("OPENAI_CHUNK_SIZE", default = 300) { it.toIntOrNull() } },
      { env("OPENAI_BACKOFF", default = 5.seconds) { it.toIntOrNull()?.seconds } },
      { env("OPENAI_MAX_RETRIES", default = 5) { it.toLongOrNull() } },
      { env("OPENAI_REQUEST_TIMEOUT", default = 30.seconds) { it.toIntOrNull()?.seconds } },
    ) { token2, baseUrl, chunkSize, backoff, maxRetries, requestTimeout ->
      OpenAIConfig(token2, baseUrl, chunkSize, requestTimeout)
    }
  }) { e: NonEmptyList<String> ->
    raise(AIError.Env.OpenAI(e))
  }

fun Raise<AIError.Env.HuggingFace>.HuggingFaceConfig(token: String? = null) =
  recover({
    zipOrAccumulate(
      { token ?: env("HF_TOKEN") },
      { env("HF_BASE_URI", default = Url("https://api-inference.huggingface.co/")) { Url(it) } }
    ) { token2, baseUrl ->
      HuggingFaceConfig(token2, baseUrl)
    }
  }) { e: NonEmptyList<String> ->
    raise(AIError.Env.HuggingFace(e))
  }

fun Raise<String>.Url(urlString: String): KUrl =
  catch({ KUrl(urlString) }) { raise(it.message ?: "Invalid url: $it") }
