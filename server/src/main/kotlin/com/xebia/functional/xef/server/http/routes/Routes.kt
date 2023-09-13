package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.xebia.functional.xef.server.models.LoginRequest
import com.xebia.functional.xef.server.models.RegisterRequest
import com.xebia.functional.xef.server.services.VectorStoreService
import com.xebia.functional.xef.server.services.UserRepositoryService
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
import io.ktor.util.pipeline.*
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
fun Routing.routes(
    client: HttpClient,
    vectorStoreService: VectorStoreService,
    userRepositoryService: UserRepositoryService
) {
    val openAiUrl = "https://api.openai.com/v1"

    post("/register") {
        try {
            val request = Json.decodeFromString<RegisterRequest>(call.receive<String>())
            val response = userRepositoryService.register(request)
            call.respond(response)
        } catch (e: Exception) {
            call.respondText(e.message ?: "Unexpected error", status = HttpStatusCode.BadRequest)
        }
    }

    post("/login") {
        try {
            val request = Json.decodeFromString<LoginRequest>(call.receive<String>())
            val response = userRepositoryService.login(request)
            call.respond(response)
        } catch (e: Exception) {
            call.respondText(e.message ?: "Unexpected error", status = HttpStatusCode.BadRequest)
        }
    }

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

private fun ApplicationCall.getToken(): String =
    principal<UserIdPrincipal>()?.name ?: throw IllegalArgumentException("No token found")


/**
 * Responds with the data and converts any potential Throwable into a 404.
 */
private suspend inline fun <reified T : Any, E : Throwable> PipelineContext<*, ApplicationCall>.response(
    block: () -> T
) = arrow.core.raise.recover<E, Unit>({
    call.respond(block())
}) {
    call.respondText(it.message ?: "Response not found", status = HttpStatusCode.NotFound)
}
