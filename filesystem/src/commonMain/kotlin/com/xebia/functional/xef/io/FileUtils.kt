package com.xebia.functional.xef.io

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import okio.*

private const val OKIO_RECOMMENDED_BUFFER_SIZE: Int = 8192

suspend fun ByteReadChannel.readFully(sink: Sink) {
  val channel = this
  sink.buffer().use {
    while (!channel.isClosedForRead) {
      val packet = channel.readRemaining(OKIO_RECOMMENDED_BUFFER_SIZE.toLong())
      while (!packet.isEmpty) {
        it.write(packet.readBytes())
      }
    }
  }
}

suspend fun ByteWriteChannel.writeAll(source: Source) {
  val channel = this
  var bytesRead: Int
  val buffer = ByteArray(OKIO_RECOMMENDED_BUFFER_SIZE)

  source.buffer().use { src ->
    while (src.read(buffer).also { bytesRead = it } != -1 && !channel.isClosedForWrite) {
      channel.writeFully(buffer, offset = 0, length = bytesRead)
      channel.flush()
    }
  }
}

suspend fun readUrlToSink(url: String, sink: Sink) {
  HttpClient { expectSuccess = true }.use {
    val response = it.get(url)
    response.bodyAsChannel().readFully(sink)
  }
}

suspend fun FileSystem.download(url: String, path: Path) {
  appendingSink(path).use {  sink ->
    readUrlToSink(url, sink)
  }
}
