package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.autoClose
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class WikipediaClient : AutoCloseable, AutoClose by autoClose() {

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

  data class SearchData(val search: String)

  data class SearchDataByPageId(val pageId: Int?)

  data class SearchDataByTitle(val title: String?)

  suspend fun search(searchData: SearchData): SearchResult {
    return http
      .get(
        "https://en.wikipedia.org/w/api.php?format=json&action=query&list=search&srsearch=${searchData.search.encodeURLQueryComponent()}"
      ) {
        contentType(ContentType.Application.Json)
      }
      .body<SearchResult>()
  }

  suspend fun searchByPageId(searchData: SearchDataByPageId): Page {
    return http
      .get(
        "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro&explaintext&redirects=1&pageids=${searchData.pageId}"
      ) {
        contentType(ContentType.Application.Json)
      }
      .body<SearchByParamResult>()
      .searchResults
      .pages
      .firstNotNullOf { it.value }
  }

  suspend fun searchByTitle(searchData: SearchDataByTitle): Page {
    return http
      .get(
        "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro&explaintext&redirects=1&titles=${searchData.title?.encodeURLQueryComponent()}"
      ) {
        contentType(ContentType.Application.Json)
      }
      .body<SearchByParamResult>()
      .searchResults
      .pages
      .firstNotNullOf { it.value }
  }

  override fun close() {
    http.close()
  }
}
