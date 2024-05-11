package com.xebia.functional.xef.aws.bedrock

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.closeable
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.bedrockruntime.BedrockRuntimeClient
import aws.sdk.kotlin.services.bedrockruntime.model.InvokeModelRequest
import aws.sdk.kotlin.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest
import com.xebia.functional.xef.aws.bedrock.conf.Environment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

suspend fun ResourceScope.sdkClient(awsEnv: Environment.Aws): BedrockRuntimeClient = closeable {
  BedrockRuntimeClient {
    region = awsEnv.regionName
    credentialsProvider =
      StaticCredentialsProvider(
        aws.smithy.kotlin.runtime.auth.awscredentials.Credentials.invoke(
          awsEnv.credentials.accessKeyId,
          awsEnv.credentials.secretAccessKey.value
        )
      )
  }
}

class SdkBedrockClient(private val client: BedrockRuntimeClient) : BedrockClient {
  override suspend fun runInference(
    requestBody: JsonElement,
    model: AwsFoundationModel
  ): ChatCompletionResponse {
    val invokeModelRequest =
      InvokeModelRequest.invoke {
        modelId = model.awsName
        accept = "application/json"
        contentType = "application/json"
        body = Json.encodeToString(requestBody).toByteArray()
      }

    val responseBody = String(client.invokeModel(invokeModelRequest).body)

    return Json.decodeFromString<ChatCompletionResponse>(responseBody)
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun runInferenceWithStream(
    requestBody: JsonElement,
    model: AwsFoundationModel
  ): Flow<ChatCompletionResponse> {
    val json = Json {
      explicitNulls = false
      ignoreUnknownKeys = true
    }
    val streamRequest =
      InvokeModelWithResponseStreamRequest.invoke {
        modelId = model.awsName
        accept = "application/json"
        contentType = "application/json"
        body = Json.encodeToString(requestBody).toByteArray()
      }

    return channelFlow {
      client.invokeModelWithResponseStream(streamRequest) { response ->
        response.body?.collect { responseStream ->
          val chunk = responseStream.asChunkOrNull()?.bytes?.toString(Charsets.UTF_8)
          chunk?.let {
            val chunkAsJson = json.decodeFromString<ChatCompletionResponse>(it)
            send(chunkAsJson)
          }
        }
      }
    }
  }
}
