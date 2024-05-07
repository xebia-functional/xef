package com.xebia.functional.xef.server.http.routes

import com.xebia.functional.openai.generated.model.ChatCompletionRequestMessage
import com.xebia.functional.xef.server.models.Token
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.ktor.resources.ResourceDescription
import guru.zoroark.tegral.openapi.ktor.resources.describeResource
import guru.zoroark.tegral.openapi.ktor.resources.postD
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.resources.*
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

fun Routing.aiRoutes(client: HttpClient) {
  val openAiUrl = "https://api.openai.com/v1"

  authenticate("auth-bearer") {
    postD<ChatCompletionRoutes> {
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

    postD<EmbeddingsRoutes> {
      val token = call.getToken()
      val context = call.receiveChannel().toByteArray()
      client.makeRequest(call, "$openAiUrl/embeddings", context, token)
    }
  }
}

@Resource("/chat/completions")
class ChatCompletionRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "AI"
      post {
        description = "Create chat completions"
        body {
          description = "The chat details"
          required = true
          json { schema<ChatCompletionRequestMessage>() }
        }
        HttpStatusCode.OK.value response { description = "Chat completions" }
      }
    })
}

@Resource("/embeddings")
class EmbeddingsRoutes {
  companion object :
    ResourceDescription by describeResource({
      tags += "AI"
      post {
        description = "Create embeddings"
        body {
          description = "The context"
          required = true
          json { schema<ByteArray>() }
        }
        HttpStatusCode.OK.value response { description = "Embeddings" }
      }
    })
}

private val conflictingRequestHeaders =
  listOf("Host", "Content-Type", "Content-Length", "Accept", "Accept-Encoding")
private val conflictingResponseHeaders = listOf("Content-Length")

private suspend fun HttpClient.makeRequest(
  call: ApplicationCall,
  url: String,
  body: ByteArray,
  token: Token
) {
  val response =
    this.request(url) {
      headers.copyFrom(call.request.headers)
      contentType(ContentType.Application.Json)
      method = HttpMethod.Post
      setBody(body)
    }
  call.response.headers.copyFrom(response.headers)
  // `response.bodyAsText()` is needed for triggering responsePipeline intercept
  call.respond(response.status, response.bodyAsText())
}

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
    }
    .execute { httpResponse ->
      call.response.headers.copyFrom(httpResponse.headers)
      call.respondOutputStream { httpResponse.bodyAsChannel().copyTo(this@respondOutputStream) }
    }
}

private fun ResponseHeaders.copyFrom(headers: Headers) =
  headers
    .entries()
    .filter { (key, _) ->
      !HttpHeaders.isUnsafe(key)
    } // setting unsafe headers results in exception
    .filterNot { (key, _) -> conflictingResponseHeaders.any { it.equals(key, true) } }
    .forEach { (key, values) -> values.forEach { value -> this.appendIfAbsent(key, value) } }

internal fun HeadersBuilder.copyFrom(headers: Headers) =
  headers
    .filter { key, _ -> !conflictingRequestHeaders.any { it.equals(key, true) } }
    .forEach { key, values -> appendMissing(key, values) }
