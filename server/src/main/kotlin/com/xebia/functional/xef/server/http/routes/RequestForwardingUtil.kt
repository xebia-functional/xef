package com.xebia.functional.xef.server.http.routes

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
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
internal suspend fun PipelineContext<Unit, ApplicationCall>.forwardToProvider(
    client: HttpClient,
    stream: Boolean = false,
    url: String = buildProviderUrlFromRequest(call.request).toString(),
    transformReqHeaders: HeadersBuilder.() -> Unit = { },
    transformReqParams: ParametersBuilder.() -> Unit = { },
): HttpResponse = if (stream) {
    client
        .prepareRequest { prepareRequest(call, url, transformReqHeaders, transformReqParams) }
        .execute { res -> call.forwardResponse(res, stream = stream); res }
} else {
    val response = client.request { prepareRequest(call, urlParam = url, transformReqHeaders, transformReqParams) }
    call.forwardResponse(response, stream = stream)
    response
}

internal suspend inline fun <reified ReqBody : Any> PipelineContext<Unit, ApplicationCall>.forwardToProvider(
    client: HttpClient,
    stream: Boolean = false,
    url: String = buildProviderUrlFromRequest(call.request).toString(),
    noinline transformReqHeaders: HeadersBuilder.() -> Unit = { },
    noinline transformReqParams: ParametersBuilder.() -> Unit = { },
    transformReqBody: (ApplicationCall) -> ReqBody,
): HttpResponse = if (stream) {
    client
        .prepareRequest { prepareRequest(call, url) }
        .execute { res -> call.forwardResponse(res, stream = stream); res }
} else {
    val response = client.request { prepareRequest(call, urlParam = url, transformReqHeaders, transformReqParams, transformReqBody) }
    call.forwardResponse(response, stream = stream)
    response
}

internal suspend inline fun <reified ReqBody : Any, reified ResBody : Any> PipelineContext<Unit, ApplicationCall>.forwardToProvider(
    client: HttpClient,
    url: String = buildProviderUrlFromRequest(call.request).toString(),
    noinline transformReqHeaders: HeadersBuilder.() -> Unit = { },
    noinline transformReqParams: ParametersBuilder.() -> Unit = { },
    transformReqBody: (ApplicationCall) -> ReqBody,
    transformResBody: (HttpResponse) -> ResBody,
): HttpResponse {
    val response = client.request { prepareRequest(call, urlParam = url, transformReqHeaders, transformReqParams, transformReqBody) }
    call.forwardResponse(response, transformResBody)
    return response
}

private suspend fun HttpRequestBuilder.prepareRequest(
    call: ApplicationCall,
    urlParam: String,
    transformHeaders: HeadersBuilder.() -> Unit = { },
    transformParams: ParametersBuilder.() -> Unit = { },
) {
    url(urlParam)
    method = call.request.httpMethod

    headers
        .apply { copyFrom(call.request.headers) } // copy headers
        .apply(transformHeaders)
    url.parameters
        .apply { appendAll(call.request.queryParameters) } // copy parameters
        .apply(transformParams)

    val body = call
        .receiveChannel()
        .toByteArray()
        .let { ByteArrayContent(it, contentType = null) }
    setBody(body)
}

private inline fun <reified T : Any> HttpRequestBuilder.prepareRequest(
    call: ApplicationCall,
    urlParam: String,
    transformHeaders: HeadersBuilder.() -> Unit = { },
    transformParams: ParametersBuilder.() -> Unit = { },
    transformBody: (ApplicationCall) -> T,
) {
    url(urlParam)
    method = call.request.httpMethod
    setBody(transformBody(call))

    headers
        .apply { copyFrom(call.request.headers) } // copy headers
        .apply(transformHeaders)
    url.parameters
        .apply { appendAll(call.request.queryParameters) } // copy parameters
        .apply(transformParams)
}

private suspend fun ApplicationCall.forwardResponse(
    providerResponse: HttpResponse,
    stream: Boolean,
) {
    if(stream) {
        respondOutputStream {
            providerResponse
                .bodyAsChannel()
                .copyTo(this@respondOutputStream)
        }
    } else {
        respond(providerResponse.status, providerResponse.readBytes())
    }
}

private suspend inline fun <reified T : Any> ApplicationCall.forwardResponse(
    providerResponse: HttpResponse,
    transformBody: (HttpResponse) -> T,
) {
    respond(providerResponse.status, transformBody(providerResponse))
}

private fun ResponseHeaders.copyFrom(headers: Headers) = headers
    .entries()
    .filter { (key, _) -> !HttpHeaders.isUnsafe(key) } // setting unsafe headers results in exception
    .filter { (key, _) -> !key.equals(HttpHeaders.ContentLength, ignoreCase = true) }
    .forEach { (key, values) ->
        values.forEach { value -> this.appendIfAbsent(key, value) }
    }

private fun HeadersBuilder.copyFrom(headers: Headers) = headers
    .filter { key, _ -> !key.equals(HttpHeaders.Host, ignoreCase = true) }
    .forEach { key, values -> appendAll(key, values) }
