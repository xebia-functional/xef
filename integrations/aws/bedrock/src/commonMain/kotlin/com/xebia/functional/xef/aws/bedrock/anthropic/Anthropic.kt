package com.xebia.functional.xef.aws.bedrock.anthropic

import aws.sdk.kotlin.services.bedrockruntime.model.InvokeModelWithResponseStreamResponse
import aws.sdk.kotlin.services.bedrockruntime.model.Trace
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequest
import com.xebia.functional.openai.generated.model.CreateChatCompletionResponse
import com.xebia.functional.openai.generated.model.CreateChatCompletionStreamResponse
import com.xebia.functional.xef.aws.bedrock.AWSBedrockModelAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject

data class Anthropic(
  override val region: String,
  override val trace: Trace = Trace.Disabled,
  override val guardrailIdentifier: String? = null,
  override val guardrailVersion: String? = null
) : AWSBedrockModelAdapter {
  override fun chatCompletionRequest(request: CreateChatCompletionRequest): JsonObject = TODO()

  override fun chatCompletionStreamRequest(request: CreateChatCompletionRequest): JsonObject =
    TODO()

  override fun chatCompletionsResponse(response: JsonObject): CreateChatCompletionResponse = TODO()

  override fun chatCompletionsSteamResponse(
    response: InvokeModelWithResponseStreamResponse
  ): Flow<CreateChatCompletionStreamResponse> = TODO()
}
