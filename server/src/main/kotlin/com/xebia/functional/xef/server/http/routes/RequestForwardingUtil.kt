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
import  io.ktor.http.URLBuilder
import io.ktor.utils.io.jvm.javaio.*

private const val OAI_URL = "https://api.openai.com"

/**
 * Retrieves the path from the incoming request and
 * combines it with the provider specific host url.
 * As we are copying the API from the providers,
 * everything including the path structure has follow the API contract.
 */
private fun buildProviderUrlFromRequest(request: ApplicationRequest) =
    URLBuilder().takeFrom(OAI_URL).apply {
    encodedPath = request.path()
}.build()

@Deprecated("")
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

/**
 * Makes a request to the provider forwarding headers and request body
 * from the incoming request.
 * The provider's response is then forwarded as a response of the server.
 * [interceptResponse] may be used to process the provider's result.
 *
 * Takes in [body] as Bytes and responds in Bytes.
 * No messing around with char sets. Just forwarding raw bytes.
 */
internal suspend fun handleForwardToProvider(client: HttpClient, call: ApplicationCall, interceptResponse: (HttpResponse) -> Unit = { }) {
    val response = client.forwardRequest(call.request)
    interceptResponse(response)
    call.forwardResponse(response)
}

internal suspend fun HttpClient.forwardRequest(
    request: ApplicationRequest,
) = request {
    url(buildProviderUrlFromRequest(request))
    method = request.httpMethod
    headers.copyFrom(request.headers) // copy headers

    val body = request
        .receiveChannel()
        .toByteArray()
        .let { ByteArrayContent(it, contentType = null) }
    setBody(body)
}

internal suspend fun ApplicationCall.forwardResponse(
    providerResponse: HttpResponse,
) {
    response.headers.copyFrom(providerResponse.headers)
    respond(providerResponse.status, providerResponse.readBytes())
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