package com.xebia.functional.xef

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

inline fun <reified A> HttpRequestBuilder.configure(token: String, request: A): Unit {
  header("Authorization", "Bearer $token")
  contentType(ContentType.Application.Json)
  setBody(request)
}
