package com.xebia.functional.openai

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

// ServerSentEvent, INTERNAL TO THIS MODULE
// RunDelta | CreateChatCompletionStreamResponse
internal suspend inline fun <reified A> FlowCollector<A>.streamEvents(
  response: HttpResponse,
  json: Json,
  prefix: String,
  end: String
) {
  val channel: ByteReadChannel = response.bodyAsChannel()
  var nextEvent: String? = null
  while (!channel.isClosedForRead) {
    val line = channel.readUTF8Line() ?: continue

    // break when we reach the end of the stream
    if (line.startsWith(end)) {
      break
    }

    // if the line is an event like "event: thread.created" we want to ensure
    // A is of type ServerSentEvent and we skip the line treating next `prefix` as a JsonObject
    // otherwise we treat the line as a json object if it starts with the prefix
    // and emit the value

    // if the line is an event like "event: thread.created" we want to ensure
    // A is of type ServerSentEvent and we skip the line treating next `prefix` as a JsonObject
    if (line.startsWith("event:")) {
      nextEvent = line.removePrefix("event:").trim()
      continue
    }
    // otherwise we treat the line as a json object if it starts with the prefix
    else if (line.startsWith(prefix) && nextEvent == null) {
      val data = line.removePrefix(prefix).trim()
      val value: A = json.decodeFromString(serializer(), data)
      emit(value)
    }
    // emit the value for the next event
    if (nextEvent != null) {
      val data = line.removePrefix(prefix).trim()
      if (data.isNotBlank()) {
        val eventData = json.decodeFromString(JsonObject.serializer(), data)
        val value: A = ServerSentEvent(event = nextEvent, data = eventData) as A
        emit(value)
      }
    }
  }

  // serverevent as A
}
