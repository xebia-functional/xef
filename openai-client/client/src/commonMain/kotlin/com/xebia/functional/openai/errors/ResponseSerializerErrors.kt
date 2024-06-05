package com.xebia.functional.openai.errors

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.content.*

suspend inline fun <reified A> HttpResponse.serializeOrThrowWithResponseInfo(): A =
  try {
    this.body() ?: throw ResponseSerializerError("Response body is null")
  } catch (e: Exception) {
    val requestBody =
      when (val content = this.request.content) {
        is OutgoingContent.ByteArrayContent -> content.bytes().decodeToString()
        is OutgoingContent.NoContent -> "NoContent"
        is OutgoingContent.ProtocolUpgrade -> "ProtocolUpgrade"
        is OutgoingContent.ReadChannelContent -> "ReadChannelContent"
        is OutgoingContent.WriteChannelContent -> "WriteChannelContent"
        else -> "UnknownContent"
      }
    throw ResponseSerializerError(
      """
      |Failed to serialize response body to ${A::class.simpleName}
      |Request URL: ${this.request.url}
      |Request Method: ${this.request.method}
      |Request Body: $requestBody
      |Response Status: ${this.status}
      |Response Headers: ${this.headers}
      |Response Body: ${this.bodyAsText()}
    """
        .trimMargin(),
      e
    )
  }
