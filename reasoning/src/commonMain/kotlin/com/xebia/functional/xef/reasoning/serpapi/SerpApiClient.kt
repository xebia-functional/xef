package com.xebia.functional.xef.reasoning.serpapi

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNames

class SerpApiClient : AutoCloseable {

    private val http: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 60_000
        }
        install(HttpRequestRetry)
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

    @Serializable
    data class SearchData(
        val q: String,
        val location: String? = null,
        val hl: String? = null,
        val gl: String? = null,
        val googleDomain: String = "google.com",
        val apiKey: String
    )

    @Serializable data class SearchResults(@JsonNames("organic_results") val searchResult: List<SearchResult>)
    @Serializable data class SearchResult(
        val title: String,
        @JsonNames("snippet") val document: String,
        val link: String
    )

    suspend fun search(searchData: SearchData): SearchResults {

        val response =
            http.get(
                "https://serpapi.com/search.json?q=${searchData.q}&location=${searchData.location}&hl=${searchData.hl}&gl=${searchData.gl}&google_domain=${searchData.googleDomain}&api_key=${searchData.apiKey}"
            ) {
                contentType(ContentType.Application.Json)
            }

        return if (response.status.isSuccess())
            response.body<SearchResults>()
        else throw SerpApiClientException(response.status, response.bodyAsText())
    }

    class SerpApiClientException(private val httpStatusCode: HttpStatusCode, private val error: String) :
        IllegalStateException("$httpStatusCode: $error")

    override fun close() {
        http.close()
    }
}