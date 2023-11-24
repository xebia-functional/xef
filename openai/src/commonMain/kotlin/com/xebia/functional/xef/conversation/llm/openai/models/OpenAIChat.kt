package com.xebia.functional.xef.conversation.llm.openai.models

import com.aallam.openai.api.chat.ChatChoice
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.chatCompletionRequest
import com.aallam.openai.api.model.ModelId
import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.conversation.llm.openai.toInternal
import com.xebia.functional.xef.conversation.llm.openai.toOpenAI
import com.xebia.functional.xef.llm.Chat
import com.xebia.functional.xef.llm.models.MaxIoContextLength
import com.xebia.functional.xef.llm.models.ModelID
import com.xebia.functional.xef.llm.models.chat.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OpenAIChat(
  private val provider: OpenAI, // TODO: use context receiver
  override val modelID: ModelID,
  override val contextLength: MaxIoContextLength,
  override val encodingType: EncodingType,
) : Chat, OpenAIModel {

  private val client = provider.defaultClient

  override fun copy(modelID: ModelID) = OpenAIChat(provider, modelID, contextLength, encodingType)

  override fun countTokens(text: String): Int =
    encoding.countTokens(text)

  override fun truncateText(text: String, maxTokens: Int): String =
    encoding.truncateText(text, maxTokens)

  override fun tokensFromMessages(messages: List<Message>): Int { // intermediary solution
    fun Encoding.countTokensFromMessages(tokensPerMessage: Int, tokensPerName: Int): Int =
      messages.sumOf { message ->
        countTokens(message.role.name) +
                countTokens(message.content) +
                tokensPerMessage +
                tokensPerName
      } + 3
    return encoding.countTokensFromMessages(tokensPerMessage = 5, tokensPerName = 5) + 10
  }

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    val response = client.chatCompletion(request.toOpenAI())
    return ChatCompletionResponse(
      id = response.id,
      `object` = response.model.id,
      created = response.created,
      model = response.model.id,
      choices = response.choices.map { it.toInternal() },
      usage = response.usage.toInternal(),
    )
  }

  override suspend fun createChatCompletions(
    request: ChatCompletionRequest
  ): Flow<ChatCompletionChunk> {
    return client.chatCompletions(request.toOpenAI()).map { it.toInternal() }
  }

  private fun ChatCompletionRequest.toOpenAI() = chatCompletionRequest {
    model = ModelId(this@OpenAIChat.modelID.value)
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
