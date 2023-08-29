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
      "German Shepherd Dog Dog Breed Information",
      "Generally considered dogkind's finest all-purpose worker, the German Shepherd Dog is a large, agile, muscular dog of noble character and high intelligence.",
      "https://www.akc.org/dog-breeds/german-shepherd-dog/"
    )

  private val searchResult2 =
    SearchResult(
      "German Shepherd Dog Breed Information & Characteristics",
      "The German Shepherds (GSDs) is a medium to large dog breed known for their intelligence, loyalty, and protective instincts. They are often used as working",
      "https://dogtime.com/dog-breeds/german-shepherd-dog"
    )

  private val searchResult3 =
    SearchResult(
      "German Shepherd - breed of dog - Britannica",
      "The German Shepherd is a breed of working dog developed in Germany from traditional herding and farm dogs. Strongly built and relatively",
      "https://www.britannica.com/animal/German-shepherd"
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
