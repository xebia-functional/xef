package com.xebia.functional.openai.errors

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.content.*

suspend inline fun <reified A> HttpResponse.serializeOrThrowWithResponseInfo(): A =
  try {
    this.body() ?: throwResponseSerializerError(title = "Response body is null")
  } catch (e: Exception) {
    throwResponseSerializerError(
      title = "Failed to serialize response body to ${A::class.simpleName}",
      cause = e
    )
  }

suspend fun HttpResponse.throwResponseSerializerError(
  title: String,
  cause: Throwable? = null
): Nothing {
  val errorInfo = ResponseErrorInfo(
    requestUrl = this.request.url.toString(),
    requestMethod = this.request.method.value,
    requestBody = extractRequestBody(this),
    responseStatus = this.status.value,
    responseHeaders = this.headers.toString(),
    responseBody = this.bodyAsText()
  )

  val message = """
    |$title
    |Request URL: ${errorInfo.requestUrl}
    |Request Method: ${errorInfo.requestMethod}
    |Request Body: ${errorInfo.requestBody}
    |Response Status: ${errorInfo.responseStatus}
    |Response Headers: ${errorInfo.responseHeaders}
    |Response Body: ${errorInfo.responseBody}
  """.trimMargin()

  throw ResponseSerializerError(message, cause, errorInfo)
}

private fun extractRequestBody(response: HttpResponse): String {
  return when (val content = response.request.content) {
    is OutgoingContent.ByteArrayContent -> content.bytes().decodeToString()
    is OutgoingContent.NoContent -> "NoContent"
    is OutgoingContent.ProtocolUpgrade -> "ProtocolUpgrade"
    is OutgoingContent.ReadChannelContent -> "ReadChannelContent"
    is OutgoingContent.WriteChannelContent -> "WriteChannelContent"
    else -> "UnknownContent"
  }
}
