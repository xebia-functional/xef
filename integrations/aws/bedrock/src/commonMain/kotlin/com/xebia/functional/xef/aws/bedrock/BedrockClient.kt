package com.xebia.functional.xef.aws.bedrock

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

interface BedrockClient {
  suspend fun runInference(
    requestBody: JsonElement,
    model: AwsFoundationModel
  ): ChatCompletionResponse

  fun runInferenceWithStream(
    requestBody: JsonElement,
    model: AwsFoundationModel
  ): Flow<SdkBedrockClient.ChatCompletionResponseEvent>
}
