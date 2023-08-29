package com.xebia.functional.xef.gcp

import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.autoClose
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class GcpClient(
  private val apiEndpoint: String,
  private val projectId: String,
  val modelId: String,
  private val token: String
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
        "https://$apiEndpoint/v1/projects/$projectId/locations/us-central1/publishers/google/models/$modelId:predict"
      ) {
        header("Authorization", "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(body)
      }

    return if (response.status.isSuccess())
      response.body<Response>().predictions.firstOrNull()?.candidates?.firstOrNull()?.content
        ?: throw AIError.NoResponse()
    else throw GcpClientException(response.status, response.bodyAsText())
  }

  class GcpClientException(val httpStatusCode: HttpStatusCode, val error: String) :
    IllegalStateException("$httpStatusCode: $error")

}
