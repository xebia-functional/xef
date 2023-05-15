package com.xebia.functional.xef

import arrow.fx.coroutines.ResourceScope
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

inline fun <reified A> HttpRequestBuilder.configure(token: String, request: A): Unit {
  header("Authorization", "Bearer $token")
  contentType(ContentType.Application.Json)
  setBody(request)
}

suspend fun ResourceScope.httpClient(engine: HttpClientEngine?, baseUrl: Url): HttpClient =
  install({
    engine?.let { HttpClient(engine) { configure(baseUrl) } } ?: HttpClient { configure(baseUrl) }
  }) { client, _ ->
    client.close()
  }

private fun HttpClientConfig<*>.configure(baseUrl: Url): Unit {
  install(ContentNegotiation) { json() }
  defaultRequest { url(baseUrl.toString()) }
}
