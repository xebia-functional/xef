package com.xebia.functional.xef.gcp.models

import com.xebia.functional.xef.gcp.GCP
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.usage.Usage
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class GcpChat(
  private val provider: GCP, // TODO: use context receiver
  override val modelID: ModelID,
  override val contextLength: MaxIoContextLength
) : Chat {

  private val client = provider.defaultClient

  override fun copy(modelID: ModelID) = GcpChat(provider, modelID, contextLength)

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    val prompt: String = request.messages.buildPrompt()
    val response: String =
      client.promptMessage(
        modelID.value,
        prompt,
        temperature = request.temperature,
        maxOutputTokens = request.maxTokens,
        topP = request.topP
      )
    return ChatCompletionResponse(
      UUID.generateUUID().toString(),
      modelID.value,
      getTimeMillis().toInt(),
      modelID.value,
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
            modelID.value,
            prompt,
            temperature = request.temperature,
            maxOutputTokens = request.maxTokens,
            topP = request.topP
          )
        emit(
          ChatCompletionChunk(
            UUID.generateUUID().toString(),
            getTimeMillis().toInt(),
            modelID.value,
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

  override fun countTokens(text: String): Int {
    TODO("Not yet implemented")
  }

  override fun truncateText(text: String, maxTokens: Int): String {
    TODO("Not yet implemented")
  }

  override fun tokensFromMessages(messages: List<Message>): Int {
    TODO("Not yet implemented")
  }
}
