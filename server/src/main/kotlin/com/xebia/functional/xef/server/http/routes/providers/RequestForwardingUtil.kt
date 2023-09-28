package com.xebia.functional.xef.server.http.routes.providers

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
import io.ktor.util.pipeline.*
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
 *
 * Takes in [body] as Bytes and responds in Bytes.
 * No messing around with char sets. Just forwarding raw bytes.
 *
 * @return provider's response
 */
internal suspend fun PipelineContext<Unit, ApplicationCall>.handleForwardToProvider(client: HttpClient): HttpResponse {
    val response = client.forwardRequest(call)
    call.forwardResponse(response)
    return response
}

private suspend fun HttpClient.forwardRequest(
    call: ApplicationCall,
) = request {
    url(buildProviderUrlFromRequest(call.request))
    method = call.request.httpMethod
    headers.copyFrom(call.request.headers) // copy headers
    url.parameters.appendAll(call.request.queryParameters) // copy parameters

    val body = call
        .receiveChannel()
        .toByteArray()
        .let { ByteArrayContent(it, contentType = null) }
    setBody(body)
}

private suspend fun ApplicationCall.forwardResponse(
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

private fun ResponseHeaders.copyFrom(headers: Headers) = headers
    .entries()
    .filter { (key, _) -> !HttpHeaders.isUnsafe(key) } // setting unsafe headers results in exception
    .forEach { (key, values) ->
        values.forEach { value -> this.appendIfAbsent(key, value) }
    }

private fun HeadersBuilder.copyFrom(headers: Headers) = headers
    .filter { key, value -> !key.equals("HOST", ignoreCase = true) }
    .forEach { key, values -> appendAll(key, values) }
