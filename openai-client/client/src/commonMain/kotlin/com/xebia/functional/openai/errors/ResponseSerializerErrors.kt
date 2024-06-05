package com.xebia.functional.openai.errors

import io.ktor.client.call.*
import io.ktor.client.statement.*

suspend inline fun <reified A> HttpResponse.serializeOrThrowWithResponseInfo(): A =
  try {
    this.body() ?: throw ResponseSerializerError("Response body is null")
  } catch (e: Exception) {
    throw ResponseSerializerError("Failed to deserialize response body:\n${bodyAsText()}", e)
  }
