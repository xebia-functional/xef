package com.xebia.functional

import arrow.fx.coroutines.ResourceScope
import io.ktor.client.HttpClient
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

suspend fun ResourceScope.httpClient(engine: HttpClientEngine, baseUrl: Url): HttpClient =
  install({
    HttpClient(engine) {
      install(ContentNegotiation) { json() }
      defaultRequest {
        url(baseUrl.toString())
      }
    }
  }) { client, _ -> client.close() }