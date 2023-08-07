package com.xebia.functional.xef.gcp.pipelines

import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.autoClose
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@OptIn(ExperimentalStdlibApi::class)
class GcpPipelinesClient(
  private val tuningLocation: String,  // Supported us-central1 or europe-west4
  private val projectId: String,
  private val token: String
) : AutoCloseable, AutoClose by autoClose() {
  private val http: HttpClient = HttpClient {
    install(HttpTimeout) {
      requestTimeoutMillis = 60_000
      connectTimeoutMillis = 60_000
    }
    install(HttpRequestRetry)
    install(ContentNegotiation) {
      json(
        Json {
          encodeDefaults = false
          isLenient = true
          ignoreUnknownKeys = true
        }
      )
    }
  }

  // https://cloud.google.com/vertex-ai/docs/reference/rest/v1/projects.locations.pipelineJobs/list#google.cloud.aiplatform.v1.PipelineService.ListPipelineJobs
  @Serializable
  private data class ListPipelineJobs(val pipelineJobs: List<PipelineJob>? = null, val nextPageToken: String? = null)

  // https://cloud.google.com/vertex-ai/docs/reference/rest/v1/projects.locations.pipelineJobs#PipelineJob
  @Serializable
  data class PipelineJob(
    val name: String,
    val displayName: String? = null,
    val createTime: String, // "2014-10-02T15:01:23Z"
    val startTime: String, // "2014-10-02T15:01:23Z"
    val endTime: String, // "2014-10-02T15:01:23Z"
    val updateTime: String, // "2014-10-02T15:01:23Z"
  )

  suspend fun list(): List<PipelineJob> {
    val response =
      http.get(
        "https://$tuningLocation-aiplatform.googleapis.com/v1/projects/$projectId/locations/$tuningLocation/pipelineJobs"
      ) {
        header("Authorization", "Bearer $token")
        contentType(ContentType.Application.Json)
      }

    return if (response.status.isSuccess())
      response.body<ListPipelineJobs>().pipelineJobs.orEmpty()
    else throw GcpClientException(response.status, response.bodyAsText())
  }

  class GcpClientException(val httpStatusCode: HttpStatusCode, val error: String) :
    IllegalStateException("$httpStatusCode: $error")

  override fun close() {
    http.close()
  }
}
