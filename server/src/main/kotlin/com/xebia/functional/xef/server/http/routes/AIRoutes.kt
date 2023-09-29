package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.server.models.Token
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive
import java.nio.charset.Charset

enum class Provider {
    OPENAI, GPT4ALL, GCP
}

fun String.toProvider(): Provider? = when (this) {
    "openai" -> Provider.OPENAI
    "gpt4all" -> Provider.GPT4ALL
    "gcp" -> Provider.GCP
    else -> Provider.OPENAI
}

@OptIn(BetaOpenAI::class)
fun Routing.aiRoutes(
    client: HttpClient
) {
    val openAiUrl = "https://api.openai.com/v1"

    authenticate("auth-bearer") {
        post("/chat/completions") {
            val token = call.getToken()
            val byteArrayBody = call.receiveChannel().toByteArray()
            val body = byteArrayBody.toString(Charsets.UTF_8)
            val data = Json.decodeFromString<JsonObject>(body)

            val isStream = data["stream"]?.jsonPrimitive?.boolean ?: false

            if (!isStream) {
                client.makeRequest(call, "$openAiUrl/chat/completions", byteArrayBody, token)
            } else {
                client.makeStreaming(call, "$openAiUrl/chat/completions", byteArrayBody, token)
            }
        }

        post("/embeddings") {
            val token = call.getToken()
            val context = call.receiveChannel().toByteArray()
            client.makeRequest(call, "$openAiUrl/embeddings", context, token)
        }
    }
}

@Deprecated("will be removed in future pr")
private suspend fun HttpClient.makeRequest(
    call: ApplicationCall,
    url: String,
    body: ByteArray,
    token: Token
) {
    val response = this.request(url) {
        headers.copyFrom(call.request.headers)
        contentType(ContentType.Application.Json)
        method = HttpMethod.Post
        setBody(body)
    }
    call.response.headers.copyFrom(response.headers)
    call.respond(response.status, response.readBytes())
}

@Deprecated("will be removed in future pr")
private suspend fun HttpClient.makeStreaming(
    call: ApplicationCall,
    url: String,
    body: ByteArray,
    token: Token
) {
    this.preparePost(url) {
        headers.copyFrom(call.request.headers)
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

@Deprecated("will be removed in future pr")
private fun ResponseHeaders.copyFrom(headers: Headers) = headers
    .entries()
    .filter { (key, _) -> !HttpHeaders.isUnsafe(key) } // setting unsafe headers results in exception
    .forEach { (key, values) ->
        values.forEach { value -> this.appendIfAbsent(key, value) }
    }

@Deprecated("will be removed in future pr")
internal fun HeadersBuilder.copyFrom(headers: Headers) = headers
    .filter { key, value -> !key.equals("HOST", ignoreCase = true) }
    .forEach { key, values -> appendAll(key, values) }

private fun ApplicationCall.getProvider(): Provider =
    request.headers["xef-provider"]?.toProvider()
        ?: Provider.OPENAI

fun ApplicationCall.getToken(): Token =
    principal<UserIdPrincipal>()?.name?.let { Token(it) } ?: throw XefExceptions.AuthorizationException("No token found")

fun ApplicationCall.getId(): Int = getInt("id")

fun ApplicationCall.getInt(field: String): Int =
    this.parameters[field]?.toInt() ?: throw XefExceptions.ValidationException("Invalid $field")

