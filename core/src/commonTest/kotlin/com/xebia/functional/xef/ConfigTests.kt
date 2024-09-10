package com.xebia.functional.xef

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.serialization.json.Json

class ConfigTests :
  StringSpec({
    val newApiToken = "new-openai-token"
    val newBaseUrl = "new-openai-url"
    val newHttpClientRetryPolicy = HttpClientRetryPolicy.NoRetry
    val newHttpClientTimeoutPolicy =
      HttpClientTimeoutPolicy(5.milliseconds, 10.milliseconds, 15.milliseconds)
    val newOrganization = "new-openai-organization"
    val newStreamingDelimiter = "new-streaming-delimiter"
    val newStreamingPrefix = "new-streaming-prefix"

    "Config Builder returns the default config if no values are provided" {
      val config = Config {}

      config shouldBe Config.Default
    }

    "Config Builder changes the default values with the provided by " {
      val config = Config {
        apiToken = newApiToken
        baseUrl = newBaseUrl
        httpClientRetryPolicy = newHttpClientRetryPolicy
        httpClientTimeoutPolicy = newHttpClientTimeoutPolicy
        json = Json.Default
        organization = newOrganization
        streamingDelimiter = newStreamingDelimiter
        streamingPrefix = newStreamingPrefix
      }

      config.apiToken shouldBe newApiToken
      config.baseUrl shouldBe newBaseUrl
      config.httpClientRetryPolicy shouldBe newHttpClientRetryPolicy
      config.httpClientTimeoutPolicy shouldBe newHttpClientTimeoutPolicy
      config.json shouldBe Json.Default
      config.organization shouldBe newOrganization
      config.streamingDelimiter shouldBe newStreamingDelimiter
      config.streamingPrefix shouldBe newStreamingPrefix
    }
  })
