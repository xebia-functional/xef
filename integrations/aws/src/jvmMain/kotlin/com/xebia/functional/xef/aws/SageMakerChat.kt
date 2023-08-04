package com.xebia.functional.xef.aws

import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

class SageMakerChat @JvmOverloads constructor(
  private val region: String,
  private val endpointName: String,
  private val client: SageMakerClient = SageMakerClient(region, endpointName)
) :
  Chat, Completion, AutoCloseable {

  override val name: String = client.endpointName
  override val modelType: ModelType =
    //TODO this needs to be parameterized somehow
    ModelType.LocalModel(client.endpointName, EncodingType.CL100K_BASE, 2048)

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response: String =
      client.promptMessage(
        request.prompt,
      )
    return CompletionResult(
      UUID.generateUUID().toString(),
      client.endpointName,
      getTimeMillis(),
      client.endpointName,
      listOf(CompletionChoice(response, 0, null, null)),
      Usage.ZERO
    )
  }

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    val prompt: String = request.messages.buildPrompt()
    val response: String =
      client.promptMessage(
        prompt
      )
    return ChatCompletionResponse(
      UUID.generateUUID().toString(),
      client.endpointName,
      getTimeMillis().toInt(),
      client.endpointName,
      Usage.ZERO,
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
            prompt
          )
        emit(
          ChatCompletionChunk(
            UUID.generateUUID().toString(),
            getTimeMillis().toInt(),
            client.endpointName,
            listOf(ChatChunk(delta = ChatDelta(Role.ASSISTANT, response))),
            Usage.ZERO,
          )
        )
      }
    }

  override fun tokensFromMessages(messages: List<Message>): Int = 0

  override fun close() {
    // todo the aws sdk clients closing is handled by smithy in the background
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
