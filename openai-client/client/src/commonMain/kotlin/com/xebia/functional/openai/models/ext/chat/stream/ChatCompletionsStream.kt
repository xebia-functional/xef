package com.xebia.functional.openai.models.ext.chat.stream

import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.openai.infrastructure.RequestConfig
import com.xebia.functional.openai.infrastructure.RequestMethod
import com.xebia.functional.openai.models.CreateChatCompletionRequest
import com.xebia.functional.openai.models.CreateChatCompletionStreamResponse
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

fun ChatApi.createChatCompletionStream(
  request: CreateChatCompletionRequest
): Flow<CreateChatCompletionStreamResponse> {
  val localVariableAuthNames = listOf("ApiKeyAuth")

  val localVariableBody = request

  val localVariableQuery = mutableMapOf<String, List<String>>()
  val localVariableHeaders = mutableMapOf<String, String>()

  val localVariableConfig =
    RequestConfig<Any?>(
      RequestMethod.POST,
      "/chat/completions",
      query = localVariableQuery,
      headers = localVariableHeaders,
      requiresAuthentication = true,
    )
  return flow {
    val statement = requestStreaming(localVariableConfig, localVariableBody, localVariableAuthNames)
    statement.execute { emitDataEvents(it) }
  }
}

private suspend fun <T : Any?> ChatApi.requestStreaming(
  requestConfig: RequestConfig<T>,
  body: CreateChatCompletionRequest? = null,
  authNames: List<String>
): HttpStatement {
  requestConfig.updateForAuth<T>(authNames)
  val headers = requestConfig.headers

  val builder =
    HttpRequestBuilder().apply {
      method = HttpMethod.Post
      url(baseUrl + requestConfig.path)
      timeout {
        requestTimeoutMillis = 60.seconds.toLong(DurationUnit.MILLISECONDS)
        socketTimeoutMillis = 60.seconds.toLong(DurationUnit.MILLISECONDS)
      }
      setBody(body?.copy(stream = true))
      contentType(ContentType.Application.Json)
      accept(ContentType.Text.EventStream)
      headers {
        append(HttpHeaders.CacheControl, "no-cache")
        append(HttpHeaders.Connection, "keep-alive")
        headers.forEach {
          if (it.key !in ApiClient.UNSAFE_HEADERS) {
            append(it.key, it.value)
          }
        }
      }
    }

  return client.preparePost(builder)
}

private const val PREFIX = "data:"
private const val END = "$PREFIX [DONE]"

private suspend inline fun FlowCollector<CreateChatCompletionStreamResponse>.emitDataEvents(
  response: HttpResponse
) {
  val channel: ByteReadChannel = response.bodyAsChannel()
  while (!channel.isClosedForRead) {
    val line = channel.readUTF8Line() ?: continue
    val value: CreateChatCompletionStreamResponse =
      when {
        line.startsWith(END) -> break
        line.startsWith(PREFIX) ->
          ApiClient.JSON_DEFAULT.decodeFromString(line.removePrefix(PREFIX))
        else -> continue
      }
    emit(value)
  }
}
