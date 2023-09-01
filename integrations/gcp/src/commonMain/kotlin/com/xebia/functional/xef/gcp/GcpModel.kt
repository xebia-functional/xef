package com.xebia.functional.xef.gcp

import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.Completion
import com.xebia.functional.xef.llm.Embeddings
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import io.ktor.util.date.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID

@OptIn(ExperimentalStdlibApi::class)
class GcpModel(modelId: String, config: GcpConfig) : Chat, Completion, AutoCloseable, Embeddings {
  private val client: GcpClient = GcpClient(modelId, config)

  override val name: String = client.modelId
  override val modelType: ModelType =
    ModelType.LocalModel(client.modelId, EncodingType.CL100K_BASE, 2048)

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response: String =
      client.promptMessage(
        request.prompt,
        temperature = request.temperature,
        maxOutputTokens = request.maxTokens,
        topP = request.topP
      )
    return CompletionResult(
      UUID.generateUUID().toString(),
      client.modelId,
      getTimeMillis(),
      client.modelId,
      listOf(CompletionChoice(response, 0, null, null)),
      Usage.ZERO, // TODO: token usage - no information about usage provided by GCP codechat model
    )
  }

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

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    fun requestToEmbedding(index: Int, it: GcpClient.EmbeddingPredictions): Embedding =
      Embedding("embedding", it.embeddings.values.map(Double::toFloat), index = index)

    val response = client.embeddings(request)
    return EmbeddingResult(
      data = response.predictions.mapIndexed(::requestToEmbedding),
      usage = usage(response),
    )
  }

  private fun usage(response: GcpClient.EmbeddingResponse) =
    Usage(
      totalTokens = response.predictions.sumOf { it.embeddings.statistics.tokenCount },
      promptTokens = null,
      completionTokens = null,
    )

  override fun tokensFromMessages(messages: List<Message>): Int = 0

  override fun close() {
    client.close()
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
