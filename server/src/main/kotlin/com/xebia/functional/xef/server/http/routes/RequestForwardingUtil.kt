package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.xef.server.models.Token
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*

const val OAI_URL = "https://api.openai.com"

internal fun buildProviderUrlFromRequest(request: ApplicationRequest) = url {
    host = OAI_URL
    encodedPath = request.path()
}

internal suspend fun HttpClient.makeRequest(
    call: ApplicationCall,
    url: String,
    body: String,
    token: Token
) {
    val response = this.request(url) {
        headers {
            bearerAuth(token.value)
        }
        contentType(ContentType.Application.Json)
        method = HttpMethod.Post
        setBody(body)
    }
    call.response.headers.copyFrom(response.headers)
    call.respond(response.status, response.body<String>())
}

internal suspend fun HttpClient.makeRequest2(
    call: ApplicationCall,
    url: String,
    body: ByteArray,
) {
    val providerResponse = this.request(url) {
        method = call.request.httpMethod
        headers.copyFrom(call.request.headers) // copy headers
        setBody(ByteArrayContent(body, contentType = null))
    }
    call.response.headers.copyFrom(providerResponse.headers)
    call.respond(providerResponse.status, providerResponse.readBytes()) // respond in bytes, no messing around with Charsets
}

internal suspend fun HttpClient.makeStreaming(
    call: ApplicationCall,
    url: String,
    body: String,
    token: Token
) {
    this.preparePost(url) {
        headers {
            bearerAuth(token.value)
        }
        contentType(ContentType.Application.Json)
        method = HttpMethod.Post
        setBody(body)
    }.execute { httpResponse ->
        call.response.headers.copyFrom(httpResponse.headers)
        call.respondOutputStream {
            httpResponse
                .bodyAsChannel()
                .copyTo(this@respondOutputStream)
        }
    }
}

internal fun ResponseHeaders.copyFrom(headers: Headers) = headers
    .entries()
    .filter { (key, _) -> !HttpHeaders.isUnsafe(key) } // setting unsafe headers results in exception
    .forEach { (key, values) ->
        values.forEach { value -> this.appendIfAbsent(key, value) }
    }

internal fun HeadersBuilder.copyFrom(headers: Headers) = headers
    .filter { key, value -> !key.equals("HOST", ignoreCase = true) }
    .forEach { key, values -> appendAll(key, values) }