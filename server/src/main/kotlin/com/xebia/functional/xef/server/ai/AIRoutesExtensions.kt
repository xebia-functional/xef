package com.xebia.functional.xef.server.ai

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*

suspend fun HttpClient.makeRequest(
    call: ApplicationCall,
    url: String,
    body: ByteArray
) {
    val response = this.request(url) {
        headers.copyFrom(call.request.headers)
        contentType(ContentType.Application.Json)
        method = HttpMethod.Post
        setBody(body)
    }
    call.response.headers.copyFrom(response.headers, "Content-Length")
    call.respond(response.status, response.readBytes())
}

suspend fun HttpClient.makeStreaming(
    call: ApplicationCall,
    url: String,
    body: ByteArray
) {
    this.preparePost(url) {
        headers.copyFrom(call.request.headers)
        method = HttpMethod.Post
        setBody(body)
    }.execute { httpResponse ->
        call.response.headers.copyFrom(httpResponse.headers, "Content-Length")
        call.respondOutputStream {
            httpResponse
                .bodyAsChannel()
                .copyTo(this@respondOutputStream)
        }
    }
}

fun ResponseHeaders.copyFrom(headers: Headers, vararg filterOut: String) = headers
    .entries()
    .filter { (key, _) -> !HttpHeaders.isUnsafe(key) } // setting unsafe headers results in exception
    .filterNot { (key, _) -> filterOut.any { it.equals(key, true) } }
    .forEach { (key, values) ->
        values.forEach { value -> this.appendIfAbsent(key, value) }
    }

internal fun HeadersBuilder.copyFrom(headers: Headers) = headers
    .filter { key, _ -> !key.equals("HOST", ignoreCase = true) }
    .forEach { key, values -> appendAll(key, values) }