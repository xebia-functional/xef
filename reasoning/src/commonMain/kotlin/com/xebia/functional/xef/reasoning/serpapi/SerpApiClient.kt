package com.xebia.functional.xef.reasoning.serpapi

import com.xebia.functional.xef.env.getenv
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

class SerpApiClient : AutoCloseable {

    private val serpApiKey: String?
    private val SERP_API_KEY_NOT_FOUND = "Missing SERP_API_KEY env var"

    init {
        serpApiKey = getenv("SERP_API_KEY") ?: throw SerpApiClientException(HttpStatusCode.Unauthorized, SERP_API_KEY_NOT_FOUND)

        if(serpApiKey.isEmpty())
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

    data class SearchData(
        val search: String,
        val location: String? = null,
        val language: String? = null,
        val region: String? = null,
        val googleDomain: String = "google.com"
    )

    @Serializable data class SearchResults(@SerialName("organic_results") val searchResults: List<SearchResult>)
    @Serializable data class SearchResult(
        val title: String,
        @SerialName("snippet") val document: String,
        @SerialName("link") val source: String
    )

    suspend fun search(searchData: SearchData): SearchResults {

        return http.get(
                "https://serpapi.com/search.json?q=${searchData.search}&location=${searchData.location}&hl=${searchData.language}&gl=${searchData.region}" +
                        "&google_domain=${searchData.googleDomain}&api_key=${serpApiKey}"
            ) {
                contentType(ContentType.Application.Json)
            }.body<SearchResults>()
    }

    class SerpApiClientException(private val httpStatusCode: HttpStatusCode, private val error: String) :
        IllegalStateException("$httpStatusCode: $error")

    override fun close() {
        http.close()
    }
}
