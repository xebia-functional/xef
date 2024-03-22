package com.xebia.functional.xef.reasoning.serpapi

import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.env.getenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SerpApiClient(private val serpApiKey: String = getenv("SERP_API_KEY") ?: "") :
  AutoCloseable, AutoClose by autoClose() {

  private val SERP_API_KEY_NOT_FOUND = "Missing SERP_API_KEY env var"

  init {
    if (serpApiKey.isBlank())
      throw SerpApiClientException(HttpStatusCode.Unauthorized, SERP_API_KEY_NOT_FOUND)
  }

  private val http: HttpClient = HttpClient {
    install(HttpTimeout) {
      requestTimeoutMillis = 60_000
      connectTimeoutMillis = 60_000
    }
    install(HttpRequestRetry) {
      maxRetries = 5
      retryIf { _, response -> !response.status.isSuccess() }
      retryOnExceptionIf { _, _ -> true }
      delayMillis { retry -> retry * 3000L }
    }
    install(ContentNegotiation) {
      json(
        Json {
          encodeDefaults = false
          isLenient = true
          ignoreUnknownKeys = true
        }
      )
    }
  }

  data class SearchData(val search: String, val engine: String? = "google")

  suspend fun search(searchData: SearchData): SearchResults {
    val response =
      http.get(
        "https://serpapi.com/search.json?q=${searchData.search.encodeURLQueryComponent()}&engine=${searchData.engine}" +
          "&api_key=${serpApiKey}"
      ) {
        contentType(ContentType.Application.Json)
      }
    return response.body<SearchResults>()
  }

  class SerpApiClientException(
    private val httpStatusCode: HttpStatusCode,
    private val error: String
  ) : IllegalStateException("$httpStatusCode: $error")

  override fun close() {
    http.close()
  }
}
