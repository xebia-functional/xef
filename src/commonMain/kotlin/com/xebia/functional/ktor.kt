package com.xebia.functional

import arrow.core.Either
import arrow.core.nonFatalOrThrow
import arrow.fx.coroutines.ResourceScope
import arrow.resilience.Schedule
import arrow.resilience.ScheduleStep
import com.xebia.functional.env.RetryConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Duration

inline fun <reified A> HttpRequestBuilder.configure(token: String, request: A): Unit {
  header("Authorization", "Bearer $token")
  contentType(ContentType.Application.Json)
  setBody(request)
}

suspend fun ResourceScope.httpClient(engine: HttpClientEngine): HttpClient =
  install({
    HttpClient(engine) {
      install(ContentNegotiation) { json() }
    }
  }) { client, _ -> client.close() }