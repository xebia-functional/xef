package com.xebia.functional.xef.gcp

import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AutoClose
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.PlatformConversation
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.store.LocalVectorStore
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
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

class GcpClient(
  val modelId: String,
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
        "https://${config.location}-aiplatform.googleapis.com/v1/projects/${config.projectId}/locations/us-central1/publishers/google/models/$modelId:predict"
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

  suspend fun embeddings(request: EmbeddingRequest): EmbeddingResponse {
    val body =
      GcpEmbeddingRequest(
        instances = request.input.map(::GcpEmbeddingInstance),
      )
    val response =
      http.post(
        "https://${config.location}-aiplatform.googleapis.com/v1/projects/${config.projectId}/locations/${config.location}/publishers/google/models/$modelId:predict"
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

  companion object {

    @JvmSynthetic
    suspend fun <A> conversation(modelId: String, config: GcpConfig, block: suspend Conversation.() -> A): A =
      block(conversation(modelId, config))

    @JvmStatic
    @JvmOverloads
    fun conversation(modelId: String, config: GcpConfig): PlatformConversation {
      val gcpEmbeddingModel =
        GcpChat(modelId, config)
      val embedding = GcpEmbeddings(gcpEmbeddingModel)
      return Conversation(LocalVectorStore(embedding))
    }

  }
}
