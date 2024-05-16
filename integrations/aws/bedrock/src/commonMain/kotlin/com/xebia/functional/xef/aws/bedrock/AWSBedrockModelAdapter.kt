package com.xebia.functional.xef.aws.bedrock

import aws.sdk.kotlin.services.bedrockruntime.model.InvokeModelWithResponseStreamResponse
import aws.sdk.kotlin.services.bedrockruntime.model.Trace
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequest
import com.xebia.functional.openai.generated.model.CreateChatCompletionResponse
import com.xebia.functional.openai.generated.model.CreateChatCompletionStreamResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

interface AWSBedrockModelAdapter {
  val region: String

  fun chatCompletionRequest(request: CreateChatCompletionRequest): JsonObject

  fun chatCompletionStreamRequest(request: CreateChatCompletionRequest): JsonObject

  fun chatCompletionsResponse(response: JsonObject): CreateChatCompletionResponse

  val trace: Trace
  val guardrailIdentifier: String?
  val guardrailVersion: String?

  fun chatCompletionsSteamResponse(
    response: InvokeModelWithResponseStreamResponse
  ): Flow<CreateChatCompletionStreamResponse>
}
