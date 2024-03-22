package com.xebia.functional.openai

import com.xebia.functional.openai.generated.api.OpenAI
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

data class Config(
  val baseUrl: String = "https://api.openai.com/v1",
  val token: String? = null,
  val org: String? = null,
  val json: Json = Json.Default,
  val streamingPrefix: String = "data:",
  val streamingDelimiter: String = "data: [DONE]"
)

/**
 * Constructor that mimics the behavior of "ApiClient", but without the additional layer in between.
 * Just simple fun on top of generated API.
 */
fun OpenAI(
  config: Config,
  httpClientEngine: HttpClientEngine? = null,
  httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
): OpenAI {
  val clientConfig: HttpClientConfig<*>.() -> Unit = {
    install(ContentNegotiation) { json(config.json) }
    install(HttpTimeout) {
      requestTimeoutMillis = 45 * 1000
      connectTimeoutMillis = 45 * 1000
      socketTimeoutMillis = 45 * 1000
    }
    install(HttpRequestRetry) {
      maxRetries = 5
      retryIf { _, response -> !response.status.isSuccess() }
      retryOnExceptionIf { _, _ -> true }
      delayMillis { retry -> retry * 1000L }
    }
    install(Logging) { level = LogLevel.NONE }
    httpClientConfig?.invoke(this)
    defaultRequest {
      url(config.baseUrl)
      config.org?.let { headers.append("org", it) }
      bearerAuth(
        config.token
          ?: TODO(
            "?: getenv(KEY_ENV_VAR) ?: throw AIError.Env.OpenAI(nonEmptyListOf(\"missing \$KEY_ENV_VAR env var\"))"
          )
      )
    }
  }
  val client = httpClientEngine?.let { HttpClient(it, clientConfig) } ?: HttpClient(clientConfig)
  return OpenAI(client, config)
}
