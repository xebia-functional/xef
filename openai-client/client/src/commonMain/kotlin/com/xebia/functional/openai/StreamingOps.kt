package com.xebia.functional.openai

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.serialization.json.Json

internal suspend inline fun <reified A> FlowCollector<A>.streamEvents(
  response: HttpResponse,
  json: Json = Json.Default,
  prefix: String = "data:",
  end: String = "$prefix [DONE]"
) {
  val channel: ByteReadChannel = response.bodyAsChannel()
  while (!channel.isClosedForRead) {
    val line = channel.readUTF8Line() ?: continue
    val value: A =
      when {
        line.startsWith(end) -> break
        line.startsWith(prefix) -> json.decodeFromString(line.removePrefix(prefix))
        else -> continue
      }
    emit(value)
  }
}
