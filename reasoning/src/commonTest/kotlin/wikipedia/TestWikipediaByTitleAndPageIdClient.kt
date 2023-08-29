package wikipedia

import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.reasoning.wikipedia.Page
import com.xebia.functional.xef.reasoning.wikipedia.SearchByParamResult
import com.xebia.functional.xef.reasoning.wikipedia.SearchByParamResults
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

class TestWikipediaByTitleAndPageIdClient : AutoCloseable, AutoClose by autoClose() {

  private val page =
    Page(
      242621,
      "List of bones of the human skeleton",
      "The human skeleton of an adult consists of around 206 bones, depending on the counting of sternum (which may alternatively be included as the manubrium, body of sternum, and the xiphoid process). It is composed of 270 bones at the time of birth, but later decreases to 206: 80 bones in the axial skeleton and 126 bones in the appendicular skeleton. 172 of 206 bones are part of a pair and the remaining 34 are unpaired. Many small accessory bones, such as sesamoid bones, are not included in this.\n\n"
    )

  private val searchByParamResults = SearchByParamResults(mapOf("242621" to page))
  private val searchByParamResult = SearchByParamResult(searchByParamResults)

  private val jsonString = Json.encodeToJsonElement(searchByParamResult)

  private val mockEngine = MockEngine { _ ->
    respond(
      content = ByteReadChannel(jsonString.toString()),
      status = HttpStatusCode.OK,
      headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
  }

  private val http =
    HttpClient(engine = mockEngine) {
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

  data class SearchDataByPageId(val pageId: Int)

  data class SearchDataByTitle(val title: String)

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
        "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro&explaintext&redirects=1&titles=${searchData.title.encodeURLQueryComponent()}"
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
