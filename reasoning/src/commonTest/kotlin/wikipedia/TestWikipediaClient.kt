package wikipedia

import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.reasoning.wikipedia.*
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

class TestWikipediaClient : AutoCloseable, AutoClose by autoClose() {

  private val searchData1 =
    SearchData(
      "Composition of the human body",
      13248239,
      33022,
      2305,
      "lipids), hydroxylapatite (<span class=\"searchmatch\">in</span> <span class=\"searchmatch\">bones</span>), carbohydrates (such as glycogen and glucose) and DNA. <span class=\"searchmatch\">In</span> terms <span class=\"searchmatch\">of</span> tissue type, <span class=\"searchmatch\">the</span> <span class=\"searchmatch\">body</span> may be analyzed into water"
    )

  private val searchData2 =
    SearchData(
      "Human skeleton",
      168848,
      21471,
      2487,
      "to around 206 <span class=\"searchmatch\">bones</span> by adulthood after some <span class=\"searchmatch\">bones</span> get fused together. <span class=\"searchmatch\">The</span> <span class=\"searchmatch\">bone</span> mass <span class=\"searchmatch\">in</span> <span class=\"searchmatch\">the</span> skeleton makes up about 14% <span class=\"searchmatch\">of</span> <span class=\"searchmatch\">the</span> total <span class=\"searchmatch\">body</span> weight (ca. 10\u201311\u00a0kg"
    )

  private val searchResults = SearchResults(listOf(searchData1, searchData2))
  private val searchResult = SearchResult(searchResults)

  private val jsonString = Json.encodeToJsonElement(searchResult)

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

  data class TestSearchData(val search: String)

  data class SearchDataByPageId(val pageId: Int)

  data class SearchDataByTitle(val title: String)

  suspend fun search(searchData: TestSearchData): SearchResult {
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
