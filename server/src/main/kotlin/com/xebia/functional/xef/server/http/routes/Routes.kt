package com.xebia.functional.xef.server.http.routes

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.configuration.PromptConfiguration
import com.xebia.functional.xef.conversation.llm.openai.*
import com.xebia.functional.xef.llm.StreamedFunction
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest as XefChatCompletionRequest
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponse
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.server.services.PersistenceService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
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
    persistenceService: PersistenceService
) {
    val openAiUrl = "https://api.openai.com/v1"

    authenticate("auth-bearer") {
        post("/chat/completions") {
            val token = call.getToken()
            val context = call.receive<String>()
            val data = Json.decodeFromString<JsonObject>(context)

            val isStream = data["stream"]?.jsonPrimitive?.boolean ?: false

            if (!isStream) {
                client.makeRequest(call, "$openAiUrl/chat/completions", context, token)
            } else {
                runBlocking {
                    client.makeStreaming(call, "$openAiUrl/chat/completions", context, token)
                }
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
    token: String)
{
    val response = this.request(url) {
        headers {
            bearerAuth(token)
        }
        contentType(ContentType.Application.Json)
        method = HttpMethod.Post
        setBody(body)
    }
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
        val channel: ByteReadChannel = httpResponse.body()
        call.respondBytesWriter(
            contentType = ContentType.Application.Json,
            status = httpResponse.status
        ) {
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    writeStringUtf8(bytes.decodeToString())
                }
            }
        }
    }
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
