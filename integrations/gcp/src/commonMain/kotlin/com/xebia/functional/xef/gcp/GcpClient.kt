package com.xebia.functional.xef.gcp

import com.xebia.functional.openai.apis.EmbeddingsApi
import com.xebia.functional.openai.models.CreateEmbeddingRequest
import com.xebia.functional.openai.models.ext.embedding.create.CreateEmbeddingRequestInput
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.autoClose
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GcpClient(
  private val config: GcpConfig,
) : AutoClose by autoClose() {
  private val http: HttpClient = jsonHttpClient()

  @Serializable
  private data class Prompt(val instances: List<Instance>, val parameters: Parameters? = null)

  @Serializable
  private data class Instance(
    val context: String? = null,
    val examples: List<Example>? = null,
    val messages: List<Message>,
  )

  @Serializable data class Example(val input: String, val output: String)

  @Serializable private data class Message(val author: String, val content: String)

  @Serializable
  private class Parameters(
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
    val topK: Int? = null,
    val topP: Double? = null
  )

  @Serializable data class Response(val predictions: List<Predictions>)

  @Serializable
  data class SafetyAttributes(
    val blocked: Boolean,
    val scores: List<String>,
    val categories: List<String>
  )

  @Serializable data class CitationMetadata(val citations: List<String>)

  @Serializable data class Candidates(val author: String?, val content: String?)

  @Serializable
  data class Predictions(
    val safetyAttributes: List<SafetyAttributes>,
    val citationMetadata: List<CitationMetadata>,
    val candidates: List<Candidates>
  )

  suspend fun promptMessage(
    modelId: String,
    prompt: String,
    temperature: Double? = null,
    maxOutputTokens: Int? = null,
    topK: Int? = null,
    topP: Double? = null
  ): String {
    val body =
      Prompt(
        listOf(Instance(messages = listOf(Message(author = "user", content = prompt)))),
        Parameters(temperature, maxOutputTokens, topK, topP)
      )
    val response =
      http.post(
        "https://${config.location.officialName}-aiplatform.googleapis.com/v1/projects/${config.projectId}/locations/${config.location.officialName}/publishers/google/models/$modelId:predict"
      ) {
        header("Authorization", "Bearer ${config.token}")
        contentType(ContentType.Application.Json)
        setBody(body)
      }

    return if (response.status.isSuccess())
      response.body<Response>().predictions.firstOrNull()?.candidates?.firstOrNull()?.content
        ?: throw AIError.NoResponse()
    else throw GcpClientException(response.status, response.bodyAsText())
  }

  @Serializable
  private data class GcpEmbeddingRequest(
    val instances: List<GcpEmbeddingInstance>,
  )

  @Serializable
  private data class GcpEmbeddingInstance(
    val content: String,
  )

  @Serializable
  data class EmbeddingResponse(
    val predictions: List<EmbeddingPredictions>,
  )

  @Serializable
  data class EmbeddingPredictions(
    val embeddings: PredictionEmbeddings,
  )

  @Serializable
  data class PredictionEmbeddings(
    val statistics: EmbeddingStatistics,
    val values: List<Double>,
  )

  @Serializable
  data class EmbeddingStatistics(
    val truncated: Boolean,
    @SerialName("token_count") val tokenCount: Int,
  )

  suspend fun embeddings(request: CreateEmbeddingRequest): EmbeddingResponse {
    val body =
      GcpEmbeddingRequest(
        instances = when (val input = request.input) {
          is CreateEmbeddingRequestInput.IntArrayArrayValue -> TODO("Does this need to be converted to String?")
          is CreateEmbeddingRequestInput.IntArrayValue -> TODO("Does this need to be converted to String?")
          is CreateEmbeddingRequestInput.StringArrayValue -> input.v.map { GcpEmbeddingInstance(it) }
          is CreateEmbeddingRequestInput.StringValue -> listOf(GcpEmbeddingInstance(input.v))
        },
      )
    val response =
      http.post(
        "https://${config.location.officialName}-aiplatform.googleapis.com/v1/projects/${config.projectId}/locations/${config.location.officialName}/publishers/google/models/${request.model}:predict"
      ) {
        header("Authorization", "Bearer ${config.token}")
        contentType(ContentType.Application.Json)
        setBody(body)
      }
    return if (response.status.isSuccess()) {
      val embedding = response.body<EmbeddingResponse>()
      if (embedding.predictions.isEmpty()) throw AIError.NoResponse()
      embedding
    } else throw GcpClientException(response.status, response.bodyAsText())
  }

  class GcpClientException(val httpStatusCode: HttpStatusCode, val error: String) :
    IllegalStateException("$httpStatusCode: $error")
}
