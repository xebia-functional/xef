package serpapi

import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.reasoning.serpapi.SearchResult
import com.xebia.functional.xef.reasoning.serpapi.SearchResults
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

class TestSerpApiClient(private val serpApiKey: String? = "SERP_API_KEY") :
  AutoCloseable, AutoClose by autoClose() {
  private val SERP_API_KEY_NOT_FOUND = "Missing SERP_API_KEY env var"

  init {
    if (serpApiKey.isNullOrBlank())
      throw SerpApiClientException(HttpStatusCode.Unauthorized, SERP_API_KEY_NOT_FOUND)
  }

  private val searchResult1 =
    SearchResult(
      "Leonardo DiCaprio's Dating History: Each Girlfriend In His ...",
      "Gigi Hadid · Camila Morrone · Camila Morrone · Camila Morrone · Nina Agdal · Rihanna · Rihanna · Kelly Rohrbach.",
      "https://www.elle.com/uk/fashion/celebrity-style/articles/g24272/leonardo-dicaprio-dating-history/"
    )

  private val searchResult2 =
    SearchResult(
      "Leonardo DiCaprio's Dating History: Gisele, Blake Lively, ...",
      "Take a look back at Leonardo DiCaprio's many high-profile relationships through the years, from Bridget Hall to Camila Morrone — photos.",
      "https://www.usmagazine.com/celebrity-news/pictures/leonardo-dicaprios-ladies-2011911/"
    )

  private val searchResult3 =
    SearchResult(
      "Leonardo DiCaprio's full dating history: All of his ex- ...",
      "Bridget Hall · Naomi Campbell · Kristen Zang · Eva Herzigová · Gisele Bündchen · Bar Refaeli · Blake Lively · Erin Heatherton.",
      "https://pagesix.com/article/leonardo-dicaprios-full-dating-history-all-of-his-ex-girlfriends/"
    )

  private val searchResults = SearchResults(listOf(searchResult1, searchResult2, searchResult3))

  private val jsonString = Json.encodeToJsonElement(searchResults)

  private val mockEngine = MockEngine { _ ->
    respond(
      content = ByteReadChannel(jsonString.toString()),
      status = HttpStatusCode.OK,
      headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
  }

  private val httpClient =
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

  data class SearchData(val search: String, val engine: String? = "google")

  suspend fun search(searchData: SearchData): SearchResults {
    val response =
      httpClient.get(
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
    httpClient.close()
  }
}
