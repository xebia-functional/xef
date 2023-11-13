package com.xebia.functional.xef.conversation.llm.openai.models

import com.xebia.functional.openai.models.CreateChatCompletionRequest
import com.xebia.functional.openai.models.CreateChatCompletionResponse
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.llm.Chat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OpenAIChat(
  private val provider: OpenAI, // TODO: use context receiver
  override val modelType: ModelType,
) : Chat {

  private val client = provider.defaultClient

  override fun copy(modelType: ModelType) = OpenAIChat(provider, modelType)

  override suspend fun createChatCompletion(
    request: CreateChatCompletionRequest
  ): CreateChatCompletionResponse {
    val response =
      client
        .return CreateChatCompletionResponse(
          id = response.id,
          `object` = response.model.id,
          created = response.created,
          model = response.model.id,
          choices = response.map { it.toInternal() },
          usage = response.usage.toInternal(),
        )
  }

  override suspend fun createChatCompletions(
    request: ChatCompletionRequest
  ): Flow<ChatCompletionChunk> {
    return client.chatCompletions(request.toOpenAI()).map { it.toInternal() }
  }

  private fun ChatCompletionRequest.toOpenAI() = chatCompletionRequest {
    model = ModelId(this@OpenAIChat.modelType.name)
    messages =
      this@toOpenAI.messages.map {
        ChatMessage(
          role = it.role.toOpenAI(),
          content = it.content,
          name = it.name,
        )
      }
    temperature = this@toOpenAI.temperature
    topP = this@toOpenAI.topP
    n = this@toOpenAI.n
    stop = this@toOpenAI.stop
    maxTokens = this@toOpenAI.maxTokens
    presencePenalty = this@toOpenAI.presencePenalty
    frequencyPenalty = this@toOpenAI.frequencyPenalty
    logitBias = this@toOpenAI.logitBias
    user = this@toOpenAI.user
  }
}

private fun ChatMessage.toInternal() =
  Message(
    role = role.toInternal(),
    content = content ?: "",
    name = name ?: "",
  )

private fun ChatChoice.toInternal() =
  Choice(
    message = message.toInternal(),
    finishReason = finishReason.value,
    index = index,
  )
