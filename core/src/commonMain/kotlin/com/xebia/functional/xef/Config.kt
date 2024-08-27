package com.xebia.functional.xef

import arrow.core.nonEmptyListOf
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
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

sealed interface HttpClientRetryPolicy {
  data object NoRetry : HttpClientRetryPolicy

  data class ExponentialBackoff(
    val backoffFactor: Double,
    val interval: Duration,
    val maxDelay: Duration,
    val maxRetries: Int
  ) : HttpClientRetryPolicy

  data class Incremental(val interval: Duration, val maxDelay: Duration, val maxRetries: Int) :
    HttpClientRetryPolicy

  private fun configureHttpRequestRetryPlugin(
    maxNumberOfRetries: Int,
    delayBlock: HttpRequestRetry.DelayContext.(Int) -> Long
  ): HttpRequestRetry.Configuration.() -> Unit = {
    maxRetries = maxNumberOfRetries
    retryIf { _, response -> !response.status.isSuccess() }
    retryOnExceptionIf { _, _ -> true }
    delayMillis(block = delayBlock)
  }

  fun applyConfiguration(): (HttpClientConfig<*>) -> Unit = { httpClientConfig ->
    when (val policy = this) {
      is ExponentialBackoff ->
        httpClientConfig.install(
          HttpRequestRetry,
          configure =
            configureHttpRequestRetryPlugin(policy.maxRetries) { retry ->
              minOf(
                policy.backoffFactor.pow(retry).toLong() * policy.interval.inWholeMilliseconds,
                policy.maxDelay.inWholeMilliseconds
              )
            }
        )
      is Incremental ->
        httpClientConfig.install(
          HttpRequestRetry,
          configure =
            configureHttpRequestRetryPlugin(policy.maxRetries) { retry ->
              minOf(
                retry * policy.interval.inWholeMilliseconds,
                policy.maxDelay.inWholeMilliseconds
              )
            }
        )
      NoRetry -> Unit
    }
  }
}

data class HttpClientTimeoutPolicy(
  val connectTimeout: Duration,
  val requestTimeout: Duration,
  val socketTimeout: Duration
) {
  val applyConfiguration: (HttpClientConfig<*>) -> Unit = { httpClientConfig ->
    httpClientConfig.install(HttpTimeout) {
      requestTimeoutMillis = requestTimeout.inWholeMilliseconds
      connectTimeoutMillis = connectTimeout.inWholeMilliseconds
      socketTimeoutMillis = socketTimeout.inWholeMilliseconds
    }
  }
}

class ConfigBuilder internal constructor(config: Config) {
  var baseUrl: String = config.baseUrl
  var httpClientRetryPolicy: HttpClientRetryPolicy = config.httpClientRetryPolicy
  var httpClientTimeoutPolicy: HttpClientTimeoutPolicy = config.httpClientTimeoutPolicy
  var apiToken: String? = config.apiToken
  var organization: String? = config.organization
  var json: Json = config.json
  var streamingPrefix: String = config.streamingPrefix
  var streamingDelimiter: String = config.streamingDelimiter

  fun build(): Config =
    Config(
      baseUrl = baseUrl,
      httpClientRetryPolicy = httpClientRetryPolicy,
      httpClientTimeoutPolicy = httpClientTimeoutPolicy,
      apiToken = apiToken,
      organization = organization,
      json = json,
      streamingPrefix = streamingPrefix,
      streamingDelimiter = streamingDelimiter
    )
}

fun Config(from: Config = Config.Default, builderAction: ConfigBuilder.() -> Unit): Config {
  val builder = ConfigBuilder(from)
  builder.builderAction()
  return builder.build()
}

data class Config(
  val baseUrl: String,
  val httpClientRetryPolicy: HttpClientRetryPolicy,
  val httpClientTimeoutPolicy: HttpClientTimeoutPolicy,
  val apiToken: String?,
  val organization: String?,
  val json: Json,
  val streamingPrefix: String,
  val streamingDelimiter: String
) {
  companion object {
    @OptIn(ExperimentalSerializationApi::class)
    val Default =
      Config(
        baseUrl = getenv(HOST_ENV_VAR) ?: "https://api.openai.com/v1/",
        httpClientRetryPolicy = HttpClientRetryPolicy.Incremental(250.milliseconds, 5.seconds, 5),
        httpClientTimeoutPolicy = HttpClientTimeoutPolicy(45.seconds, 45.seconds, 45.seconds),
        json =
          Json {
            ignoreUnknownKeys = true
            prettyPrint = false
            isLenient = true
            explicitNulls = false
            classDiscriminator = TYPE_DISCRIMINATOR
          },
        organization = getenv(ORG_ENV_VAR),
        streamingDelimiter = "data: [DONE]",
        streamingPrefix = "data:",
        apiToken = null
      )

    const val TYPE_DISCRIMINATOR = "_type_"
  }
}

private const val ORG_ENV_VAR = "OPENAI_ORG"
private const val HOST_ENV_VAR = "OPENAI_HOST"
private const val KEY_ENV_VAR = "OPENAI_TOKEN"

/**
 * Constructor that mimics the behavior of "ApiClient", but without the additional layer in between.
 * Just simple fun on top of generated API.
 */
fun OpenAI(
  config: Config = Config.Default,
  httpClientEngine: HttpClientEngine? = null,
  httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
  logRequests: Boolean = false
): OpenAI {
  val token =
    config.apiToken
      ?: getenv(KEY_ENV_VAR)
      ?: throw AIError.Env.OpenAI(nonEmptyListOf("missing $KEY_ENV_VAR env var"))
  val clientConfig: HttpClientConfig<*>.() -> Unit = {
    install(ContentNegotiation) { json(config.json) }
    install(Logging) { level = if (logRequests) LogLevel.ALL else LogLevel.INFO }
    config.httpClientRetryPolicy.applyConfiguration().invoke(this)
    config.httpClientTimeoutPolicy.applyConfiguration.invoke(this)
    httpClientConfig?.invoke(this)
    defaultRequest {
      url(config.baseUrl)
      config.organization?.let { headers.append("OpenAI-Organization", it) }
      bearerAuth(token)
    }
  }
  val client = httpClientEngine?.let { HttpClient(it, clientConfig) } ?: HttpClient(clientConfig)
  return OpenAI(
    client,
    OpenAIConfig(
      baseUrl = config.baseUrl,
      token = token,
      org = config.organization,
      json = config.json,
      streamingPrefix = config.streamingPrefix,
      streamingDelimiter = config.streamingDelimiter
    )
  )
}
