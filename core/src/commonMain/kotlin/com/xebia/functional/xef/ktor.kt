package com.xebia.functional.xef

import arrow.fx.coroutines.ResourceScope
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
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

suspend fun ResourceScope.httpClient(baseUrl: Url): HttpClient =
  install({
    HttpClient {
      install(HttpTimeout)
      install(ContentNegotiation) { json() }
      install(HttpRequestRetry)
      defaultRequest { url(baseUrl.toString()) }
    }
  }) { client, _ ->
    client.close()
  }
