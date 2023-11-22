package com.xebia.functional.openai.models.ext.chat.stream

import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.openai.infrastructure.RequestConfig
import com.xebia.functional.openai.infrastructure.RequestMethod
import com.xebia.functional.openai.models.CreateChatCompletionRequest
import com.xebia.functional.openai.models.CreateChatCompletionStreamResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*

fun ChatApi.createChatCompletionStream(
  baseUrl: String,
  request: CreateChatCompletionRequest
): Flow<CreateChatCompletionStreamResponse> {
//  val builder =
//    HttpRequestBuilder().apply {
//      method = HttpMethod.Post
//      url(path = "chat/completions")
//      takeFrom(URLBuilder(baseUrl))
//      setBody(streamingRequestAsJson(request))
//      contentType(ContentType.Application.Json)
//      accept(ContentType.Text.EventStream)
//      headers {
//        append(HttpHeaders.CacheControl, "no-cache")
//        append(HttpHeaders.Connection, "keep-alive")
//      }
//    }
  val localVariableAuthNames = listOf<String>("ApiKeyAuth")

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
    val response = requestStreaming(localVariableConfig, localVariableBody, localVariableAuthNames)
    emitDataEvents(response)
  }
}

private suspend fun <T : Any?> ChatApi.requestStreaming(
  requestConfig: RequestConfig<T>,
  body: Any? = null,
  authNames: kotlin.collections.List<String>
): HttpResponse {
  requestConfig.updateForAuth<T>(authNames)
  val headers = requestConfig.headers

  return client.request {
    this.url {
      contentType(ContentType.Application.Json)
      accept(ContentType.Text.EventStream)
      this.takeFrom(URLBuilder(baseUrl))
      appendPath(requestConfig.path.trimStart('/').split('/'))
      requestConfig.query.forEach { query ->
        query.value.forEach { value -> parameter(query.key, value) }
      }
    }
    this.method = requestConfig.method.httpMethod
    headers
      .filter { header -> !ApiClient.UNSAFE_HEADERS.contains(header.key) }
      .forEach { header -> this.header(header.key, header.value) }

    header(HttpHeaders.CacheControl, "no-cache")
    header(HttpHeaders.Connection, "keep-alive")

    if (
      requestConfig.method in listOf(RequestMethod.PUT, RequestMethod.POST, RequestMethod.PATCH)
    ) {
      this.setBody(body)
    }
  }
}

private const val PREFIX = "data:"
private const val END = "$PREFIX [DONE]"

private suspend inline fun <reified T> FlowCollector<T>.emitDataEvents(response: HttpResponse) {
  val channel: ByteReadChannel = response.body()
  while (!channel.isClosedForRead) {
    val line = channel.readUTF8Line() ?: continue
    val value: T =
      when {
        line.startsWith(END) -> break
        line.startsWith(PREFIX) ->
          ApiClient.JSON_DEFAULT.decodeFromString(line.removePrefix(PREFIX))
        else -> continue
      }
    emit(value)
  }
}

private suspend fun <T : Any> HttpClient.execute(
  builder: HttpRequestBuilder,
  block: suspend (response: HttpResponse) -> T
) {
  try {
    HttpStatement(builder = builder, client = this).execute(block)
  } catch (e: Exception) {
    throw e // TODO handle exception
  }
}

