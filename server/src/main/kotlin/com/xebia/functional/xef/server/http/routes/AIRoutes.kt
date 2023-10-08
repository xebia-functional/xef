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
            val data = call.receive<JsonObject>()
            val isStream = data["stream"]?.jsonPrimitive?.boolean ?: false
            forwardToProvider(client, stream = isStream, url = "$openAiUrl/chat/completions")
        }

        post("/embeddings") {
            forwardToProvider(client, url = "$openAiUrl/embeddings")
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

