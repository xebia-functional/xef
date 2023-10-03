package com.xebia.functional.xef.conversation.llm.openai.models

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.EncodingType
import com.xebia.functional.xef.conversation.llm.openai.toInternal
import com.xebia.functional.xef.conversation.llm.openai.toOpenAI
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.chat.ChatCompletionChunk
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.functions.FunChatCompletionRequest
import com.xebia.functional.xef.llm.models.functions.FunctionCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class OpenAIFunChat(
  override val modelID: com.xebia.functional.xef.llm.models.ModelID,
  private val client: OpenAI,
  override val encodingType: EncodingType,
) : ChatWithFunctions, OpenAIModel {

  override suspend fun estimateTokens(request: FunChatCompletionRequest): Int {
    // TODO: improve token estimation, difficulty here: estimate tokens taken by the functions
    fun Encoding.countTokensFromMessages(tokensPerMessage: Int, tokensPerName: Int): Int =
      request.messages.sumOf { message ->
        countTokens(message.role.name) +
          countTokens(message.content) +
          tokensPerMessage +
          tokensPerName
      } + 3
    return encoding.countTokensFromMessages(tokensPerMessage = 5, tokensPerName = 5) + 10 + 200
  }

  override suspend fun createChatCompletionWithFunctions(
    request: FunChatCompletionRequest
  ): ChatCompletionResponseWithFunctions {
    val openAIRequest = chatCompletionRequest {
      model = ModelId(modelID.value)
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
    request: FunChatCompletionRequest
  ): Flow<ChatCompletionChunk> {
    val clientRequest = chatCompletionRequest {
      model = ModelId(modelID.value)
      messages = request.messages.map { it.toOpenAI() }
      temperature = request.temperature
      topP = request.topP
      n = request.n
      stop = request.stop
      maxTokens = request.maxTokens
      presencePenalty = request.presencePenalty
      frequencyPenalty = request.frequencyPenalty
      logitBias = request.logitBias
      user = request.user

      functions = request.functions.map { it.toOpenAI() }
      functionCall =
        request.functionCall?.get("name")?.let { FunctionMode.Named(it) } ?: FunctionMode.Auto
    }

    return client.chatCompletions(clientRequest).map { it.toInternal() }
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
