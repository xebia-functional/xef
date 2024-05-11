package com.xebia.functional.xef.aws.bedrock

import arrow.fx.coroutines.timeInMillis
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonElement
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class BedrockChat(
  private val client: BedrockClient
) : Chat {

  override suspend fun createChatCompletion(
    createChatCompletionRequest: CreateChatCompletionRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): CreateChatCompletionResponse =
    client.runInference(
      anthropicRequest(createChatCompletionRequest),
      awsFoundationModel(createChatCompletionRequest)
    ).let { response ->
      createChatCompletionResponse(response, createChatCompletionRequest)
    }

  private fun awsFoundationModel(createChatCompletionRequest: CreateChatCompletionRequest): AwsFoundationModel =
    (AwsFoundationModel.entries.find { it.awsName == createChatCompletionRequest.model.value }
      ?: throw IllegalArgumentException("Model not found"))

  private fun createChatCompletionResponse(
    response: ChatCompletionResponse,
    createChatCompletionRequest: CreateChatCompletionRequest
  ): CreateChatCompletionResponse = CreateChatCompletionResponse(
    id = UUID.generateUUID().toString(),
    choices = listOf(
      CreateChatCompletionResponseChoicesInner(
        index = 0,
        finishReason = CreateChatCompletionResponseChoicesInner.FinishReason.stop,
        message = ChatCompletionResponseMessage(
          content = response.completion,
          role = ChatCompletionResponseMessage.Role.assistant,
        ),
        logprobs = null

      )
    ),
    created = (timeInMillis() / 1000).toInt(),
    model = createChatCompletionRequest.model.value,
    `object` = CreateChatCompletionResponse.Object.chat_completion,
    usage = response.invocationMetrics?.run {
      CompletionUsage(
        completionTokens = outputTokenCount,
        promptTokens = inputTokenCount,
        totalTokens = inputTokenCount + outputTokenCount
      )
    }
  )

  private fun anthropicRequest(createChatCompletionRequest: CreateChatCompletionRequest): JsonElement =
    anthropic {
      this.topP = createChatCompletionRequest.topP?.toInt() ?: defaultTopP
      this.temperature = createChatCompletionRequest.temperature ?: defaultTemperature
      this.maxTokens = createChatCompletionRequest.maxTokens ?: defaultMaxTokens
      this.stopSequences = when (val stop = createChatCompletionRequest.stop) {
        is CreateChatCompletionRequestStop.CaseString -> listOf(stop.value)
        is CreateChatCompletionRequestStop.CaseStrings -> stop.value
        null -> defaultStopSequences
      }
      this.prompt = createChatCompletionRequest.messages.mapNotNull { message ->
        when (message) {
          is ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage ->
            "Assistant: ${message.value.content}"

          is ChatCompletionRequestMessage.CaseChatCompletionRequestFunctionMessage ->
            "Function: ${message.value.content}"

          is ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage ->
            "System: ${message.value.content}"

          is ChatCompletionRequestMessage.CaseChatCompletionRequestToolMessage ->
            "Tool: ${message.value.content}"

          is ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage ->
            "Human: ${message.value.content}\nAssistant:"

          else -> null
        }
      }.joinToString(separator = "\n")
    }


  override fun createChatCompletionStream(
    createChatCompletionRequest: CreateChatCompletionRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<CreateChatCompletionStreamResponse> =
    client.runInferenceWithStream(
      anthropic {

      },
      awsFoundationModel(createChatCompletionRequest)
    ).map {
      CreateChatCompletionStreamResponse(
        id = UUID.generateUUID().toString(),
        choices = listOf(
          CreateChatCompletionStreamResponseChoicesInner(
            index = 0,
            finishReason = CreateChatCompletionStreamResponseChoicesInner.FinishReason.stop,
            delta = ChatCompletionStreamResponseDelta(
              content = it.completion,
              role = ChatCompletionStreamResponseDelta.Role.assistant,
            ),
            logprobs = null
          )
        ),
        created = (timeInMillis() / 1000).toInt(),
        model = createChatCompletionRequest.model.value,
        `object` = CreateChatCompletionStreamResponse.Object.chat_completion_chunk,
      )
    }
}
