package com.xebia.functional.xef.aws

import aws.sdk.kotlin.services.sagemaker.model.DescribeEndpointRequest
import aws.sdk.kotlin.services.sagemakerruntime.SageMakerRuntimeClient
import aws.sdk.kotlin.services.sagemakerruntime.endpoints.EndpointParameters
import aws.sdk.kotlin.services.sagemakerruntime.model.InvokeEndpointRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * A client for the AWS SageMaker service.
 * Code examples about the underlying client:
 * https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/kotlin/services/sagemaker
 */
class SageMakerClient(
  private val region: String,
  internal val endpointName: String,
  private val client: SageMakerRuntimeClient =
    SageMakerRuntimeClient { this.region = region }
) {
  suspend fun promptMessage(
    input: String
  ): String {
    val endpointResponse = client.invokeEndpoint(
      InvokeEndpointRequest {
        endpointName = this@SageMakerClient.endpointName
        contentType = "application/json"
        body = Json.encodeToString(InferenceRequest.serializer(), InferenceRequest(input, input.length)).toByteArray()
      }
    )
    return endpointResponse.body?.toString(Charsets.UTF_8) ?: error("No response body")
  }
}

@Serializable
data class InferenceRequest(val text: String, @SerialName("text_length")  val textLengt: Int)
