package com.xebia.functional.openai.models.ext.chat.stream

import com.xebia.functional.openai.apis.ChatApi
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

fun ChatApi.createChatCompletionStream(request: CreateChatCompletionRequest): Flow<CreateChatCompletionStreamResponse> {
  val builder = HttpRequestBuilder().apply {
    method = HttpMethod.Post
    url(path = "/chat/completions")
    setBody(streamingRequestAsJson(request))
    contentType(ContentType.Application.Json)
    accept(ContentType.Text.EventStream)
    headers {
      append(HttpHeaders.CacheControl, "no-cache")
      append(HttpHeaders.Connection, "keep-alive")
    }
  }
  return flow {
    client.execute(builder) { response -> emitDataEvents(response) }
  }
}

private val json = Json {
  isLenient = true
  ignoreUnknownKeys = true
}

private const val PREFIX = "data:"
private const val END = "$PREFIX [DONE]"

private suspend inline fun <reified T> FlowCollector<T>.emitDataEvents(response: HttpResponse) {
  val channel: ByteReadChannel = response.body()
  while (!channel.isClosedForRead) {
    val line = channel.readUTF8Line() ?: continue
    val value: T = when {
      line.startsWith(END) -> break
      line.startsWith(PREFIX) -> json.decodeFromString(line.removePrefix(PREFIX))
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

private inline fun <reified T> streamingRequestAsJson(serializable: T): JsonElement {
  val enableStream = "stream" to JsonPrimitive(true)
  val json = json.encodeToJsonElement(serializable)
  val map = json.jsonObject.toMutableMap().also { it += enableStream }
  return JsonObject(map)
}
