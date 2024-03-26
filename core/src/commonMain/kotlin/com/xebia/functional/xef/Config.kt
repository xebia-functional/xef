package com.xebia.functional.xef

import com.xebia.functional.openai.Config as OpenAIConfig
import com.xebia.functional.openai.generated.api.OpenAI
import com.xebia.functional.xef.env.getenv
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

data class Config(
  val baseUrl: String = getenv(HOST_ENV_VAR) ?: "https://api.openai.com/v1/",
  val token: String? = getenv(KEY_ENV_VAR),
  val org: String? = getenv(ORG_ENV_VAR),
  val json: Json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
    explicitNulls = false
  },
  val streamingPrefix: String = "data:",
  val streamingDelimiter: String = "data: [DONE]"
) {
  companion object {
    private const val KEY_ENV_VAR = "OPENAI_TOKEN"
    private const val ORG_ENV_VAR = "OPENAI_ORG"
    private const val HOST_ENV_VAR = "OPENAI_HOST"
  }
}

/**
 * Constructor that mimics the behavior of "ApiClient", but without the additional layer in between.
 * Just simple fun on top of generated API.
 */
fun OpenAI(
  config: Config = Config(),
  httpClientEngine: HttpClientEngine? = null,
  httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
  logRequests: Boolean = false
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
    install(Logging) {
      level =
        if (logRequests) {
          LogLevel.ALL
        } else LogLevel.NONE
    }
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
  return OpenAI(
    client,
    OpenAIConfig(
      baseUrl = config.baseUrl,
      token = config.token,
      org = config.org,
      json = config.json,
      streamingPrefix = config.streamingPrefix,
      streamingDelimiter = config.streamingDelimiter
    )
  )
}
