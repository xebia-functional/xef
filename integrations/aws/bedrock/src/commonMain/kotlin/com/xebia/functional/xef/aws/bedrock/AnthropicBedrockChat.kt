package com.xebia.functional.xef.aws.bedrock

import arrow.fx.coroutines.mapIndexed
import arrow.fx.coroutines.timeInMillis
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.aws.bedrock.SdkBedrockClient.ChatCompletionResponseEvent.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class AnthropicBedrockChat(private val client: BedrockClient) : Chat {

  override suspend fun createChatCompletion(
    createChatCompletionRequest: CreateChatCompletionRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): CreateChatCompletionResponse =
    client
      .runInference(
        anthropicRequest(createChatCompletionRequest),
        awsFoundationModel(createChatCompletionRequest)
      )
      .let { response -> createChatCompletionResponse(response, createChatCompletionRequest) }

  private fun awsFoundationModel(
    createChatCompletionRequest: CreateChatCompletionRequest
  ): AwsFoundationModel =
    (AwsFoundationModel.entries.find { it.awsName == createChatCompletionRequest.model.value }
      ?: throw IllegalArgumentException("Model not found"))

  private fun createChatCompletionResponse(
    response: ChatCompletionResponse,
    createChatCompletionRequest: CreateChatCompletionRequest
  ): CreateChatCompletionResponse =
    CreateChatCompletionResponse(
      id = response.id,
      choices =
        response.content.mapIndexed { index, message ->
          CreateChatCompletionResponseChoicesInner(
            index = index,
            finishReason = CreateChatCompletionResponseChoicesInner.FinishReason.stop,
            message =
              ChatCompletionResponseMessage(
                content = message.text,
                role = ChatCompletionResponseMessage.Role.assistant
              ),
            logprobs = null
          )
        },
      created = (timeInMillis() / 1000).toInt(),
      model = createChatCompletionRequest.model.value,
      `object` = CreateChatCompletionResponse.Object.chat_completion,
      usage =
        response.usage?.run {
          CompletionUsage(
            completionTokens = outputTokens ?: 0,
            promptTokens = inputTokens ?: 0,
            totalTokens =
              inputTokens?.let { input -> outputTokens?.let { output -> input + output } } ?: 0
          )
        }
    )

  private fun anthropicRequest(
    createChatCompletionRequest: CreateChatCompletionRequest
  ): JsonElement {
    val systemMessage =
      createChatCompletionRequest.messages
        .filterIsInstance<ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage>()
        .joinToString("\n") { it.value.content }
    val remainingMessages =
      createChatCompletionRequest.messages.filterNot {
        it is ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage
      }
    // the remaining messages should not have messages of the same role one after the other
    // so we need to alternate between assistant and user messages
    // and ensure that the first message is a user message
    // additionally if messages of the same role are consecutive they should be combined into a
    // single message

    val cleanedMessages = enforceAnthropicFormat(remainingMessages)

    return AnthropicMessagesRequestBody(
        system = systemMessage,
        topP = createChatCompletionRequest.topP,
        temperature = createChatCompletionRequest.temperature,
        maxTokens = createChatCompletionRequest.maxTokens ?: 3000,
        stopSequences =
          when (val stop = createChatCompletionRequest.stop) {
            is CreateChatCompletionRequestStop.CaseString -> listOf(stop.value)
            is CreateChatCompletionRequestStop.CaseStrings -> stop.value
            null -> null
          },
        messages = cleanedMessages,
        //      tools = createChatCompletionRequest.tools?.map {
        //        AnthropicChatCompletionTool(
        //          name = it.function.name,
        //          description = it.function.description ?: "",
        //          inputSchema = it.function.parameters ?: JsonObject(emptyMap())
        //        )
        //      }, //TODO enable when bedrock support anthropic tool calling
      )
      .let {
        Config.DEFAULT.json.encodeToJsonElement(AnthropicMessagesRequestBody.serializer(), it)
      }
  }

  private fun enforceAnthropicFormat(
    remainingMessages: List<ChatCompletionRequestMessage>
  ): List<ChatCompletionRequestMessage> =
    remainingMessages.fold(mutableListOf()) { acc, message ->
      if (acc.isEmpty()) {
        acc.add(message)
      } else {
        val lastMessage = acc.last()
        val lastContent = extractContent(lastMessage)
        val messageContent = extractContent(message)
        if (
          lastMessage is ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage &&
            message is ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage
        ) {
          acc[acc.lastIndex] =
            ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage(
              value =
                ChatCompletionRequestUserMessage(
                  role = ChatCompletionRequestUserMessage.Role.user,
                  content =
                    ChatCompletionRequestUserMessageContent.CaseString(
                      value = "${lastContent}\n${messageContent}"
                    )
                )
            )
        } else if (
          lastMessage is ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage &&
            message is ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage
        ) {
          acc[acc.lastIndex] =
            ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage(
              value =
                ChatCompletionRequestSystemMessage(
                  role = ChatCompletionRequestSystemMessage.Role.system,
                  content = "${lastContent}\n${messageContent}"
                )
            )
        } else if (
          lastMessage is ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage &&
            message is ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage
        ) {
          acc[acc.lastIndex] =
            ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(
              value =
                ChatCompletionRequestAssistantMessage(
                  role = ChatCompletionRequestAssistantMessage.Role.assistant,
                  content = "${lastContent}\n${messageContent}"
                )
            )
        } else {
          acc.add(message)
        }
      }
      acc
    }

  private fun extractContent(lastMessage: ChatCompletionRequestMessage): String? {
    return when (lastMessage) {
      is ChatCompletionRequestMessage.CaseChatCompletionRequestUserMessage ->
        when (val content = lastMessage.value.content) {
          is ChatCompletionRequestUserMessageContent.CaseString -> content.value
          is ChatCompletionRequestUserMessageContent.CaseChatCompletionRequestMessageContentParts ->
            content.value.joinToString("\n") {
              when (it) {
                is ChatCompletionRequestMessageContentPart.CaseChatCompletionRequestMessageContentPartImage ->
                  it.value.imageUrl.url
                is ChatCompletionRequestMessageContentPart.CaseChatCompletionRequestMessageContentPartText ->
                  it.value.text
              }
            }
        }
      is ChatCompletionRequestMessage.CaseChatCompletionRequestSystemMessage ->
        lastMessage.value.content
      is ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage ->
        lastMessage.value.content
      is ChatCompletionRequestMessage.CaseChatCompletionRequestFunctionMessage ->
        lastMessage.value.content
      is ChatCompletionRequestMessage.CaseChatCompletionRequestToolMessage ->
        lastMessage.value.content
    }
  }

  override fun createChatCompletionStream(
    createChatCompletionRequest: CreateChatCompletionRequest,
    configure: HttpRequestBuilder.() -> Unit
  ): Flow<CreateChatCompletionStreamResponse> =
    client
      .runInferenceWithStream(
        anthropicRequest(createChatCompletionRequest),
        awsFoundationModel(createChatCompletionRequest)
      )
      .mapIndexed { index, event ->
        CreateChatCompletionStreamResponse(
          id = UUID.generateUUID().toString(),
          choices =
            when (event) {
              is ContentBlockDelta ->
                listOf(
                  CreateChatCompletionStreamResponseChoicesInner(
                    index = event.index,
                    finishReason = null,
                    delta =
                      ChatCompletionStreamResponseDelta(
                        content = event.delta.text,
                        role = ChatCompletionStreamResponseDelta.Role.assistant,
                      ),
                    logprobs = null
                  )
                )
              is ContentBlockStart -> emptyList()
              is ContentBlockStop -> emptyList()
              is MessageDelta ->
                listOf(
                  CreateChatCompletionStreamResponseChoicesInner(
                    index =
                      0, // TODO: index is always 0 for now, need to update when we have multiple
                    // messages
                    finishReason = null,
                    delta =
                      ChatCompletionStreamResponseDelta(
                        content = event.delta.text,
                        role = ChatCompletionStreamResponseDelta.Role.assistant,
                      ),
                    logprobs = null
                  )
                )
              is MessageStart -> emptyList()
              is MessageStop ->
                listOf(
                  CreateChatCompletionStreamResponseChoicesInner(
                    index =
                      0, // TODO: index is always 0 for now, need to update when we have multiple
                    // messages
                    finishReason = CreateChatCompletionStreamResponseChoicesInner.FinishReason.stop,
                    delta =
                      ChatCompletionStreamResponseDelta(
                        content = "",
                        role = ChatCompletionStreamResponseDelta.Role.assistant,
                      ),
                    logprobs = null
                  )
                )
              is Ping -> emptyList()
            },
          created = (timeInMillis() / 1000).toInt(),
          model = createChatCompletionRequest.model.value,
          `object` = CreateChatCompletionStreamResponse.Object.chat_completion_chunk,
        )
      }
}
