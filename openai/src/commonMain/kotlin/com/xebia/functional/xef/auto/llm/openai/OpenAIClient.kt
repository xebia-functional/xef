package com.xebia.functional.xef.auto.llm.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.chat.ChatCompletionRequest as OpenAIChatCompletionRequest
import com.aallam.openai.api.completion.Choice as OpenAIChoice
import com.aallam.openai.api.completion.CompletionRequest as OpenAICompletionRequest
import com.aallam.openai.api.completion.TextCompletion
import com.aallam.openai.api.completion.completionRequest
import com.aallam.openai.api.core.Usage as OpenAIUsage
import com.aallam.openai.api.embedding.EmbeddingRequest as OpenAIEmbeddingRequest
import com.aallam.openai.api.embedding.EmbeddingResponse
import com.aallam.openai.api.embedding.embeddingRequest
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.image.imageCreation
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI as OpenAIClient
import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.functions.FunctionCall as FnCall
import com.xebia.functional.xef.llm.models.images.ImageGenerationUrl
import com.xebia.functional.xef.llm.models.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import kotlinx.serialization.json.Json

class OpenAIModel(
  private val openAI: OpenAI,
  override val name: String,
  override val modelType: ModelType
) : Chat, ChatWithFunctions, Images, Completion, Embeddings, AutoCloseable {

  private val client = OpenAIClient(
    token = openAI.token,
    host = when (val host = openAI.host) {
      is OpenAIHost.OpenAI -> com.aallam.openai.client.OpenAIHost.OpenAI
      is OpenAIHost.Azure -> com.aallam.openai.client.OpenAIHost.azure(
        resourceName = host.resourceName,
        deploymentId = host.deploymentId,
        apiVersion = host.apiVersion,
      )
    },
  )

  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    val response = client.completion(toCompletionRequest(request))
    return completionResult(response)
  }

  @OptIn(BetaOpenAI::class)
  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    val response = client.chatCompletion(toChatCompletionRequest(request))
    return chatCompletionResult(response)
  }

  @OptIn(BetaOpenAI::class)
  override suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequestWithFunctions
  ): ChatCompletionResponseWithFunctions {
    val response = client.chatCompletion(toChatCompletionRequestWithFunctions(request))
    return chatCompletionResultWithFunctions(response)
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val response = client.embeddings(toEmbeddingRequest(request))
    return embeddingResult(response)
  }

  @OptIn(BetaOpenAI::class)
  override suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse {
    val response = client.imageURL(toImageCreationRequest(request))
    return imageResult(response)
  }

  private fun toCompletionRequest(request: CompletionRequest): OpenAICompletionRequest =
    completionRequest {
      model = ModelId(request.model)
      user = request.user
      prompt = request.prompt
      suffix = request.suffix
      maxTokens = request.maxTokens
      temperature = request.temperature
      topP = request.topP
      n = request.n
      logprobs = request.logprobs
      echo = request.echo
      stop = request.stop
      presencePenalty = request.presencePenalty
      frequencyPenalty = request.frequencyPenalty
      bestOf = request.bestOf
      logitBias = request.logitBias
    }

  private fun completionResult(response: TextCompletion): CompletionResult =
    CompletionResult(
      id = response.id,
      `object` = response.model.id,
      created = response.created,
      model = response.model.id,
      choices = response.choices.map { completionChoice(it) },
      usage = usage(response.usage)
    )

  private fun completionChoice(it: OpenAIChoice): CompletionChoice =
    CompletionChoice(
      it.text,
      it.index,
      null,
      it.finishReason,
    )

  private fun usage(usage: OpenAIUsage?): Usage =
    Usage(
      promptTokens = usage?.promptTokens,
      completionTokens = usage?.completionTokens,
      totalTokens = usage?.totalTokens,
    )

  @OptIn(BetaOpenAI::class)
  private fun chatCompletionResult(response: ChatCompletion): ChatCompletionResponse =
    ChatCompletionResponse(
      id = response.id,
      `object` = response.model.id,
      created = response.created,
      model = response.model.id,
      choices = response.choices.map { chatCompletionChoice(it) },
      usage = usage(response.usage)
    )

  @OptIn(BetaOpenAI::class)
  private fun chatCompletionResultWithFunctions(
    response: ChatCompletion
  ): ChatCompletionResponseWithFunctions =
    ChatCompletionResponseWithFunctions(
      id = response.id,
      `object` = response.model.id,
      created = response.created,
      model = response.model.id,
      choices = response.choices.map { chatCompletionChoiceWithFunctions(it) },
      usage = usage(response.usage)
    )

  @OptIn(BetaOpenAI::class)
  private fun chatCompletionChoiceWithFunctions(choice: ChatChoice): ChoiceWithFunctions =
    ChoiceWithFunctions(
      message =
        choice.message?.let {
          MessageWithFunctionCall(
            role = it.role.role,
            content = it.content,
            name = it.name,
            functionCall = it.functionCall?.let { FnCall(it.name, it.arguments) }
          )
        },
      finishReason = choice.finishReason,
      index = choice.index,
    )

  @OptIn(BetaOpenAI::class)
  private fun chatCompletionChoice(choice: ChatChoice): Choice =
    Choice(
      message =
        choice.message?.let {
          Message(
            role = toRole(it),
            content = it.content ?: "",
            name = it.name ?: "",
          )
        },
      finishReason = choice.finishReason,
      index = choice.index,
    )

  @OptIn(BetaOpenAI::class)
  private fun toRole(it: ChatMessage) =
    when (it.role) {
      ChatRole.User -> Role.USER
      ChatRole.Assistant -> Role.ASSISTANT
      ChatRole.System -> Role.SYSTEM
      ChatRole.Function -> Role.SYSTEM
      else -> Role.ASSISTANT
    }

  @OptIn(BetaOpenAI::class)
  private fun fromRole(it: Role) =
    when (it) {
      Role.USER -> ChatRole.User
      Role.ASSISTANT -> ChatRole.Assistant
      Role.SYSTEM -> ChatRole.System
    }

  @OptIn(BetaOpenAI::class)
  private fun toChatCompletionRequest(request: ChatCompletionRequest): OpenAIChatCompletionRequest =
    chatCompletionRequest {
      model = ModelId(request.model)
      messages =
        request.messages.map {
          ChatMessage(
            role = fromRole(it.role),
            content = it.content,
            name = it.name,
          )
        }
      temperature = request.temperature
      topP = request.topP
      n = request.n
      stop = request.stop
      maxTokens = request.maxTokens
      presencePenalty = request.presencePenalty
      frequencyPenalty = request.frequencyPenalty
      logitBias = request.logitBias
      user = request.user
    }

  @OptIn(BetaOpenAI::class)
  private fun toChatCompletionRequestWithFunctions(
    request: ChatCompletionRequestWithFunctions
  ): OpenAIChatCompletionRequest = chatCompletionRequest {
    model = ModelId(request.model)
    messages =
      request.messages.map {
        ChatMessage(role = fromRole(it.role), content = it.content, name = it.name)
      }

    functions =
      request.functions.map {
        val schema = Json.parseToJsonElement(it.parameters)
        ChatCompletionFunction(
          name = it.name,
          description = it.description,
          parameters = Parameters(schema),
        )
      }
    temperature = request.temperature
    topP = request.topP
    n = request.n
    stop = request.stop
    maxTokens = request.maxTokens
    presencePenalty = request.presencePenalty
    frequencyPenalty = request.frequencyPenalty
    logitBias = request.logitBias
    user = request.user
    functionCall = request.functionCall["name"]?.let { FunctionMode.Named(it) } ?: FunctionMode.Auto
  }

  private fun embeddingResult(response: EmbeddingResponse): EmbeddingResult =
    EmbeddingResult(
      data =
        response.embeddings.map {
          Embedding(
            `object` = "embedding",
            embedding = it.embedding.map { it.toFloat() },
            index = it.index
          )
        },
      usage = usage(response.usage)
    )

  private fun toEmbeddingRequest(request: EmbeddingRequest): OpenAIEmbeddingRequest =
    embeddingRequest {
      model = ModelId(request.model)
      input = request.input
      user = request.user
    }

  @OptIn(BetaOpenAI::class)
  private fun imageResult(response: List<ImageURL>): ImagesGenerationResponse =
    ImagesGenerationResponse(data = response.map { ImageGenerationUrl(it.url) })

  @OptIn(BetaOpenAI::class)
  private fun toImageCreationRequest(request: ImagesGenerationRequest): ImageCreation =
    imageCreation {
      prompt = request.prompt
      n = request.numberImages
      size = ImageSize(request.size)
      user = request.user
    }

  override fun tokensFromMessages(messages: List<Message>): Int {
    fun Encoding.countTokensFromMessages(tokensPerMessage: Int, tokensPerName: Int): Int =
      messages.sumOf { message ->
        countTokens(message.role.name) +
          countTokens(message.content) +
          tokensPerMessage +
          tokensPerName
      } + 3

    fun fallBackTo(fallbackModel: Chat, paddingTokens: Int): Int {
      return fallbackModel.tokensFromMessages(messages) + paddingTokens
    }

    return when (this) {
      openAI.GPT_3_5_TURBO_FUNCTIONS ->
        // paddingToken = 200: reserved for functions
        fallBackTo(fallbackModel = openAI.GPT_3_5_TURBO_0301, paddingTokens = 200)
      openAI.GPT_3_5_TURBO ->
        // otherwise if the model changes, it might later fail
        fallBackTo(fallbackModel = openAI.GPT_3_5_TURBO_0301, paddingTokens = 5)
      openAI.GPT_4,
      openAI.GPT_4_32K ->
        // otherwise if the model changes, it might later fail
        fallBackTo(fallbackModel = openAI.GPT_4_0314, paddingTokens = 5)
      openAI.GPT_3_5_TURBO_0301 ->
        modelType.encoding.countTokensFromMessages(tokensPerMessage = 4, tokensPerName = 0)
      openAI.GPT_4_0314 ->
        modelType.encoding.countTokensFromMessages(tokensPerMessage = 3, tokensPerName = 2)
      else -> fallBackTo(fallbackModel = openAI.GPT_3_5_TURBO_0301, paddingTokens = 20)
    }
  }

  override fun close() {
    client.close()
  }
}
