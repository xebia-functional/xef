package com.xebia.functional.xef.gcp.models

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.gcp.GcpClient
import com.xebia.functional.xef.gcp.GcpConfig
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.usage.Usage
import io.ktor.client.*
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class GcpChat(
  override val modelType: ModelType,
  private val config: GcpConfig,
) : Chat {

  private val client: GcpClient = autoClose { GcpClient(modelType.name, config) }

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    val prompt: String = request.messages.buildPrompt()
    val response: String =
      client.promptMessage(
        prompt,
        temperature = request.temperature,
        maxOutputTokens = request.maxTokens,
        topP = request.topP
      )
    return ChatCompletionResponse(
      UUID.generateUUID().toString(),
      client.modelId,
      getTimeMillis().toInt(),
      client.modelId,
      Usage.ZERO, // TODO: token usage - no information about usage provided by GCP
      listOf(Choice(Message(Role.ASSISTANT, response, Role.ASSISTANT.name), null, 0)),
    )
  }

  /** GCP currently doesn't support streaming responses */
  override suspend fun createChatCompletions(
    request: ChatCompletionRequest
  ): Flow<ChatCompletionChunk> =
    with(request) {
      return flow {
        val prompt: String = messages.buildPrompt()
        val response =
          client.promptMessage(
            prompt,
            temperature = request.temperature,
            maxOutputTokens = request.maxTokens,
            topP = request.topP
          )
        emit(
          ChatCompletionChunk(
            UUID.generateUUID().toString(),
            getTimeMillis().toInt(),
            client.modelId,
            listOf(ChatChunk(delta = ChatDelta(Role.ASSISTANT, response))),
            Usage
              .ZERO, // TODO: token usage - no information about usage provided by GCP for codechat
            // model
          )
        )
      }
    }

  private fun List<Message>.buildPrompt(): String {
    val messages: String =
      joinToString("") { message ->
        when (message.role) {
          Role.SYSTEM -> message.content
          Role.USER -> "\n### Human: ${message.content}"
          Role.ASSISTANT -> "\n### Response: ${message.content}"
        }
      }
    return "$messages\n### Response:"
  }
}
