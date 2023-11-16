package com.xebia.functional.xef.utils

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import kotlin.coroutines.CoroutineContext

class TestHttpResponse(context: CoroutineContext, statusCode: Int) : io.ktor.client.statement.HttpResponse() {
  override val call: HttpClientCall = HttpClientCall(HttpClient())
  @InternalAPI override val content: ByteReadChannel = ByteReadChannel.Empty
  override val coroutineContext: CoroutineContext = context
  override val headers: Headers = Headers.Empty
  override val requestTime: GMTDate = GMTDate.START
  override val responseTime: GMTDate = GMTDate.START
  override val status: HttpStatusCode = HttpStatusCode(statusCode, "Mocked status")
  override val version: HttpProtocolVersion = HttpProtocolVersion.HTTP_2_0
}
