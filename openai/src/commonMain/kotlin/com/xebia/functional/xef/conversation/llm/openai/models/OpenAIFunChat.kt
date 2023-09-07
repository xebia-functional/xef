package com.xebia.functional.xef.conversation.llm.openai.models

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.conversation.llm.openai.toInternal
import com.xebia.functional.xef.conversation.llm.openai.toOpenAI
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.chat.ChatCompletionChunk
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.functions.FunctionCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class OpenAIFunChat(
  override val modelType: ModelType,
  private val client: OpenAI,
) : ChatWithFunctions {

  override suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequest
  ): ChatCompletionResponseWithFunctions {
    val openAIRequest = chatCompletionRequest {
      model = ModelId(request.model)
      messages = request.messages.map { it.toOpenAI() }
      functions = request.functions.map { it.toOpenAI() }
      temperature = request.temperature
      topP = request.topP
      n = request.n
      stop = request.stop
      maxTokens = request.maxTokens
      presencePenalty = request.presencePenalty
      frequencyPenalty = request.frequencyPenalty
      logitBias = request.logitBias
      user = request.user
      functionCall =
        request.functionCall?.get("name")?.let { FunctionMode.Named(it) } ?: FunctionMode.Auto
    }

    fun fromOpenAI(it: ChatMessage): MessageWithFunctionCall =
      MessageWithFunctionCall(
        role = it.role.role,
        content = it.content,
        name = it.name,
        functionCall = it.functionCall?.let { FunctionCall(it.name, it.arguments) }
      )

    fun choiceWithFunctions(choice: ChatChoice): ChoiceWithFunctions =
      ChoiceWithFunctions(
        message = fromOpenAI(choice.message),
        finishReason = choice.finishReason.value,
        index = choice.index,
      )

    fun fromResponse(response: ChatCompletion): ChatCompletionResponseWithFunctions =
      ChatCompletionResponseWithFunctions(
        id = response.id,
        `object` = response.model.id,
        created = response.created,
        model = response.model.id,
        choices = response.choices.map { choiceWithFunctions(it) },
        usage = response.usage.toInternal()
      )

    return fromResponse(client.chatCompletion(openAIRequest))
  }

  override suspend fun createChatCompletionsWithFunctions(
    request: ChatCompletionRequest
  ): Flow<ChatCompletionChunk> {
    return client.chatCompletions(request.toOpenAI()).map { it.toInternal() }
  }
}

private fun CFunction.toOpenAI() =
  ChatCompletionFunction(
    name = name,
    description = description,
    parameters = Parameters(Json.parseToJsonElement(parameters)),
  )

private fun Message.toOpenAI() =
  ChatMessage(
    role = role.toOpenAI(),
    content = content,
    name = name,
  )

private fun ChatCompletionRequest.toOpenAI() = chatCompletionRequest {
  model = ModelId(this@toOpenAI.model)
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

  if (this@toOpenAI.functions.isNotEmpty())
    functions =
      this@toOpenAI.functions.map {
        ChatCompletionFunction(
          name = it.name,
          description = it.description,
          parameters = Parameters(Json.parseToJsonElement(it.parameters)),
        )
      }
  if (this@toOpenAI.functionCall != null)
    functionCall =
      this@toOpenAI.functionCall?.get("name")?.let { FunctionMode.Named(it) } ?: FunctionMode.Auto
}
