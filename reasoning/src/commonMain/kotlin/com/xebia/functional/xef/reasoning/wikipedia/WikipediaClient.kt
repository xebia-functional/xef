package com.xebia.functional.xef.reasoning.wikipedia

import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.autoClose
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

  data class SearchDataByParam(val pageId: Int? = null, val title: String? = null)

  @Serializable data class SearchResult(@SerialName("query") val searchResults: SearchResults)

  @Serializable data class SearchResults(@SerialName("search") val searches: List<Search>)

  @Serializable
  data class Search(
    val title: String,
    @SerialName("pageid") val pageId: Int,
    @SerialName("size") val size: Int,
    @SerialName("wordcount") val wordCount: Int,
    @SerialName("snippet") val document: String
  )

  @Serializable
  data class SearchByParamResult(@SerialName("query") val searchResults: SearchByParamResults)

  @Serializable data class SearchByParamResults(val pages: Map<String, Page>)

  @Serializable
  data class Page(
    @SerialName("pageid") val pageId: Int,
    val title: String,
    @SerialName("extract") val document: String
  )

  suspend fun search(searchData: SearchData): SearchResult {
    return http
      .get(
        "https://en.wikipedia.org/w/api.php?format=json&action=query&list=search&srsearch=${searchData.search.encodeURLQueryComponent()}"
      ) {
        contentType(ContentType.Application.Json)
      }
      .body<SearchResult>()
  }

  suspend fun searchByPageId(searchData: SearchDataByParam): Page {
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

  suspend fun searchByTitle(searchData: SearchDataByParam): Page {
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
