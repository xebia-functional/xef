package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.server.http.routes.providers.forwardToProvider
import com.xebia.functional.xef.server.http.routes.providers.makeRequest
import com.xebia.functional.xef.server.http.routes.providers.makeStreaming
import com.xebia.functional.xef.server.models.Token
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
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
fun Routing.aiRoutes(
    client: HttpClient
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

            //forwardToProvider(client, stream = isStream) //TODO
        }

        post("/embeddings") {
            val token = call.getToken()
            val context = call.receive<String>()
            client.makeRequest(call, "$openAiUrl/embeddings", context, token)

            //forwardToProvider(client, stream = isStream) //TODO
        }
    }
}

private fun ApplicationCall.getProvider(): Provider =
    request.headers["xef-provider"]?.toProvider()
        ?: Provider.OPENAI

fun ApplicationCall.getToken(): Token =
    principal<UserIdPrincipal>()?.name?.let { Token(it) } ?: throw XefExceptions.AuthorizationException("No token found")

fun ApplicationCall.getId(): Int = getInt("id")

fun ApplicationCall.getInt(field: String): Int =
    this.parameters[field]?.toInt() ?: throw XefExceptions.ValidationException("Invalid $field")

