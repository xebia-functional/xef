package ai.xef.langchain4j

import ai.xef.Chat
import com.xebia.functional.xef.llm.*
import dev.langchain4j.agent.tool.ToolParameters
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.data.message.*
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.output.FinishReason
import dev.langchain4j.model.output.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

abstract class Langchain4JChat(
  val chat: ChatLanguageModel,
  val streamingChat: StreamingChatLanguageModel,
  override val modelName: String,
  override val tokenPaddingSum: Int,
  override val tokenPadding: Int,
  override val maxContextLength: Int
) : Chat {
  override suspend fun createChatCompletion(createChatCompletionRequest: CreateChatCompletionRequest): CreateChatCompletionResponse {
    val messages: List<ChatMessage> = chatMessages(createChatCompletionRequest)
    val toolSpecifications: List<ToolSpecification> = createChatCompletionRequest.tools.map {
      toolSpecification(it)
    }
    val response: Response<AiMessage> = chat.generate(messages, toolSpecifications)
    return chatCompletionResponse(response)
  }

  private fun toolSpecification(it: ChatCompletionTool): ToolSpecification =
    ToolSpecification.builder()
      .name(it.function.name)
      .description(it.function.description)
      .parameters(it.function.schema.toToolParameters())
      .build()

  private fun JsonObject.toToolParameters(): ToolParameters {
    TODO()
    return ToolParameters.builder()
      .build()
  }

  private fun chatMessages(createChatCompletionRequest: CreateChatCompletionRequest) =
    createChatCompletionRequest.messages.map {
      it.chatMessage()
    }

  private fun ChatCompletionRequestMessage.chatMessage(): ChatMessage =
    when (role) {
      Role.system -> SystemMessage(content)
      Role.user -> UserMessage(content)
      Role.assistant -> AiMessage(content)
      Role.tool -> {
        val toolCallResults = toolCallResults
        if (toolCallResults == null) throw IllegalArgumentException("Tool messages must have toolCallResults")
        else ToolExecutionResultMessage(
          toolCallResults.toolCallId,
          toolCallResults.toolCallName,
          toolCallResults.result
        )
      }
    }

  private fun chatCompletionResponse(response: Response<AiMessage>): CreateChatCompletionResponse =
    CreateChatCompletionResponse(
      id = UUID.generateUUID().toString(),
      created = System.currentTimeMillis().toInt(),
      model = modelName,
      choices = choicesInners(response)
    )

  private fun choicesInners(response: Response<AiMessage>): List<CreateChatCompletionResponseChoicesInner> =
    listOf(
      CreateChatCompletionResponseChoicesInner(
        finishReason = finishReason(response),
        message = ChatCompletionResponseMessage(
          content = response.content().text(),
          role = Role.assistant,
          toolCalls = toolCalls(response)
        )
      )
    )

  private fun toolCalls(response: Response<AiMessage>): List<ChatCompletionMessageToolCall> =
    if (response.content().hasToolExecutionRequests()) {
      response.content().toolExecutionRequests().map {
        ChatCompletionMessageToolCall(
          id = it.id(),
          function = ToolCall(
            functionName = it.name(),
            arguments = it.arguments()
          )
        )
      }
    } else emptyList()

  private fun finishReason(response: Response<AiMessage>): com.xebia.functional.xef.llm.FinishReason =
    when (response.finishReason()) {
      FinishReason.STOP -> com.xebia.functional.xef.llm.FinishReason.stop
      FinishReason.LENGTH -> com.xebia.functional.xef.llm.FinishReason.length
      FinishReason.TOOL_EXECUTION -> com.xebia.functional.xef.llm.FinishReason.tool_calls
      FinishReason.CONTENT_FILTER -> com.xebia.functional.xef.llm.FinishReason.content_filter
      FinishReason.OTHER -> com.xebia.functional.xef.llm.FinishReason.other
      null -> com.xebia.functional.xef.llm.FinishReason.other
    }

  override fun createChatCompletionStream(createChatCompletionRequest: CreateChatCompletionRequest): Flow<CreateChatCompletionStreamResponse> {
    TODO("Not yet implemented")
  }

}

