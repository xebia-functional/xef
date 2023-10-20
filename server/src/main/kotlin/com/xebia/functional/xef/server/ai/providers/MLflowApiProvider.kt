package com.xebia.functional.xef.server.ai.providers

import com.xebia.functional.xef.server.ai.copyFrom
import com.xebia.functional.xef.server.ai.providers.mlflow.*
import com.xebia.functional.xef.server.models.exceptions.XefExceptions
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
class MLflowApiProvider(
  private val client: HttpClient,
  private val gatewayUrl: String,
  private val pathProvider: PathProvider
) : ApiProvider {

  private val json = Json { explicitNulls = false }

  private suspend inline fun <reified Req, reified R1, reified R2> PipelineContext<
    Unit, ApplicationCall
  >
    .makeRequest(
    model: String,
    requestData: Req,
    path: (String, UserIdPrincipal) -> String?,
    toXef: R1.() -> R2
  ) {
    val principal =
      call.principal<UserIdPrincipal>() ?: throw XefExceptions.AuthorizationException("")
    val resolvedPath = path(model, principal)
    resolvedPath?.let {
      val newBody = json.encodeToString(requestData).toByteArray(Charsets.UTF_8)
      val response =
        client.post("$gatewayUrl$resolvedPath") {
          accept(ContentType.Application.Json)
          contentType(ContentType.Application.Json)
          setBody(newBody)
        }
      call.response.headers.copyFrom(response.headers, HttpHeaders.ContentLength)
      if (response.status.isSuccess()) {
        val stringResponseBody = response.bodyAsText()
        val responseData = Json.decodeFromString<R1>(stringResponseBody)
        call.respond(
          response.status,
          json.encodeToString(responseData.toXef()).toByteArray(Charsets.UTF_8)
        )
      } else call.respond(response.status, response.readBytes())
    }
      ?: let { call.respond(HttpStatusCode.NotFound, "Model not found") }
  }

  override suspend fun PipelineContext<Unit, ApplicationCall>.chatRequest() {
    val requestBody = call.receiveChannel().toByteArray()
    val stringRequestBody = requestBody.toString(Charsets.UTF_8)
    val requestData = Json.decodeFromString<XefChatRequest>(stringRequestBody)
    if (requestData.stream == true) {
      call.respond(HttpStatusCode.NotImplemented, "Stream not supported")
    } else {
      makeRequest(
        requestData.model,
        requestData.toMLflow(),
        pathProvider::chatPath,
        MLflowChatResponse::toXef
      )
    }
  }

  override suspend fun PipelineContext<Unit, ApplicationCall>.embeddingsRequest() {
    val requestBody = call.receiveChannel().toByteArray()
    val stringRequestBody = requestBody.toString(Charsets.UTF_8)
    val requestData = Json.decodeFromString<XefEmbeddingsRequest>(stringRequestBody)
    if (requestData.encodingFormat == XefEncodingFormat.BASE64) {
      call.respond(HttpStatusCode.NotImplemented, "Base64 format not supported")
    } else
      makeRequest(
        requestData.model,
        requestData.toMLflow(),
        pathProvider::embeddingsPath,
        MLflowEmbeddingsResponse::toXef
      )
  }
}

suspend fun mlflowApiProvider(client: HttpClient, gatewayUrl: String): MLflowApiProvider {
  val pathProvider = mlflowPathProvider(client, gatewayUrl)
  return MLflowApiProvider(client, gatewayUrl, pathProvider)
}
