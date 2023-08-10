package com.xebia.functional.xef.gcp.pipelines

import com.xebia.functional.xef.auto.AutoClose
import com.xebia.functional.xef.auto.autoClose
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
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

  @Serializable
  enum class PipelineState {
    PIPELINE_STATE_UNSPECIFIED,
    PIPELINE_STATE_QUEUED,
    PIPELINE_STATE_PENDING,
    PIPELINE_STATE_RUNNING,
    PIPELINE_STATE_SUCCEEDED,
    PIPELINE_STATE_FAILED,
    PIPELINE_STATE_CANCELLING,
    PIPELINE_STATE_CANCELLED,
    PIPELINE_STATE_PAUSED
  }

  // https://cloud.google.com/vertex-ai/docs/reference/rest/v1/projects.locations.pipelineJobs#PipelineJob
  @Serializable
  data class PipelineJob(
    val name: String,
    val displayName: String,
    val createTime: Instant,
    val startTime: Instant?,
    val endTime: Instant?,
    val updateTime: Instant,
    val state: PipelineState
  )

  @Serializable
  data class CreatePipelineJob(
    val displayName: String,
    val runtimeConfig: RuntimeConfig,
    val templateUri: String
  )

  @Serializable
  data class RuntimeConfig(
    val gcsOutputDirectory: String,
    val parameterValues: ParameterValues
  )

  @Serializable
  data class ParameterValues(
    @SerialName("project")
    val project: String,
    @SerialName("model_display_name")
    val modelDisplayName: String,
    @SerialName("dataset_uri")
    val datasetUri: String,
    @SerialName("location")
    val location: String,
    @SerialName("large_model_reference")
    val largeModelReference: String,
    @SerialName("train_steps")
    val trainSteps: String,
    @SerialName("learning_rate_multiplier")
    val learningRateMultiplier: String
  )

  @Serializable
  data class Operation(
    val name: String,
    val done: Boolean,
    val error: OperationStatus?
  )

  @Serializable
  data class OperationStatus(
    val code: Int,
    val message: String
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

  suspend fun get(pipelineJobName: String): PipelineJob? {
    val response =
      http.get(
        "https://$tuningLocation-aiplatform.googleapis.com/v1/projects/$projectId/locations/$tuningLocation/pipelineJobs/$pipelineJobName"
      ) {
        header("Authorization", "Bearer $token")
        contentType(ContentType.Application.Json)
      }

    return if (response.status.isSuccess()) response.body<PipelineJob?>()
    else throw GcpClientException(response.status, response.bodyAsText())
  }

  suspend fun create(pipelineJobId: String?, pipelineJob: CreatePipelineJob): PipelineJob? {
    val response =
      http.post(
        "https://$tuningLocation-aiplatform.googleapis.com/v1/projects/$projectId/locations/$tuningLocation/pipelineJobs"
      ) {
        header("Authorization", "Bearer $token")
        contentType(ContentType.Application.Json)
        parameter("pipelineJobId", pipelineJobId)
        setBody(pipelineJob)
      }

    return if (response.status.isSuccess()) response.body<PipelineJob?>()
    else throw GcpClientException(response.status, response.bodyAsText())
  }

  suspend fun cancel(pipelineJobName: String): Unit {
    val response =
      http.post(
        "https://$tuningLocation-aiplatform.googleapis.com/v1/projects/$projectId/locations/$tuningLocation/pipelineJobs/$pipelineJobName:cancel"
      ) {
        header("Authorization", "Bearer $token")
        contentType(ContentType.Application.Json)
      }

    return if (response.status.isSuccess()) {}
    else throw GcpClientException(response.status, response.bodyAsText())
  }

  suspend fun delete(pipelineJobName: String): Operation {
    val response =
      http.delete(
        "https://$tuningLocation-aiplatform.googleapis.com/v1/projects/$projectId/locations/$tuningLocation/pipelineJobs/$pipelineJobName"
      ) {
        header("Authorization", "Bearer $token")
        contentType(ContentType.Application.Json)
      }

    return if (response.status.isSuccess()) response.body<Operation>()
    else throw GcpClientException(response.status, response.bodyAsText())
  }

  class GcpClientException(val httpStatusCode: HttpStatusCode, val error: String) :
    IllegalStateException("$httpStatusCode: $error")

  override fun close() {
    http.close()
  }
}
