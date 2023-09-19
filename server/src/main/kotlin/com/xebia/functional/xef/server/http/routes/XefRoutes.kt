package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import com.xebia.functional.xef.server.services.VectorStoreService
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
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonPrimitive

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
fun Routing.genAIRoutes(
    client: HttpClient,
    vectorStoreService: VectorStoreService
) {
    val openAiUrl = "https://api.openai.com/v1"

    authenticate("auth-bearer") {
        post("/chat/completions") {
            val token = call.getToken()
            val body = call.receive<String>()
            val data = Json.decodeFromString<JsonObject>(body)

            val isStream = data["stream"]?.jsonPrimitive?.boolean ?: false

            if (!isStream) {
                client.makeRequest(call, "$openAiUrl/chat/completions", body, token)
            } else {
                client.makeStreaming(call, "$openAiUrl/chat/completions", body, token)
            }
        }

        post("/embeddings") {
            val token = call.getToken()
            val context = call.receive<String>()
            client.makeRequest(call, "$openAiUrl/embeddings", context, token)
        }
    }
}

private suspend fun HttpClient.makeRequest(
    call: ApplicationCall,
    url: String,
    body: String,
    token: String
) {
    val response = this.request(url) {
        headers {
            bearerAuth(token)
        }
        contentType(ContentType.Application.Json)
        method = HttpMethod.Post
        setBody(body)
    }
    call.response.headers.copyFrom(response.headers)
    call.respond(response.status, response.body<String>())
}

private suspend fun HttpClient.makeStreaming(
    call: ApplicationCall,
    url: String,
    body: String,
    token: String
) {
    this.preparePost(url) {
        headers {
            bearerAuth(token)
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

private fun ApplicationCall.getProvider(): Provider =
    request.headers["xef-provider"]?.toProvider()
        ?: Provider.OPENAI

fun ApplicationCall.getToken(): String =
    principal<UserIdPrincipal>()?.name ?: throw XefExceptions.AuthorizationException("No token found")

fun ApplicationCall.getId(): Int = getInt("id")

fun ApplicationCall.getInt(field: String): Int =
    this.parameters[field]?.toInt() ?: throw XefExceptions.ValidationException("Invalid $field")

