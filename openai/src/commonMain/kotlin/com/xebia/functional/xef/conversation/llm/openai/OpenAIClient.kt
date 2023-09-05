package com.xebia.functional.xef.conversation.llm.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.LegacyOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.chat.ChatChunk as OpenAIChatChunk
import com.aallam.openai.api.chat.ChatCompletionChunk as OpenAIChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest as OpenAIChatCompletionRequest
import com.aallam.openai.api.chat.ChatDelta as OpenAIChatDelta
import com.aallam.openai.api.completion.Choice as OpenAIChoice
import com.aallam.openai.api.completion.CompletionRequest as OpenAICompletionRequest
import com.aallam.openai.api.completion.completionRequest
import com.aallam.openai.api.core.Usage as OpenAIUsage
import com.aallam.openai.api.embedding.Embedding as OpenAIEmbedding
import com.aallam.openai.api.embedding.EmbeddingRequest as OpenAIEmbeddingRequest
import com.aallam.openai.api.embedding.embeddingRequest
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.imageCreation
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI as OpenAIClient
import com.aallam.openai.client.OpenAIHost
import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.llm.*
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.llm.models.chat.ChatChunk
import com.xebia.functional.xef.llm.models.chat.ChatCompletionChunk
import com.xebia.functional.xef.llm.models.chat.ChatDelta
import com.xebia.functional.xef.llm.models.embeddings.Embedding
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingRequest
import com.xebia.functional.xef.llm.models.embeddings.EmbeddingResult
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.functions.FunctionCall as FnCall
import com.xebia.functional.xef.llm.models.images.ImageGenerationUrl
import com.xebia.functional.xef.llm.models.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.llm.models.text.CompletionChoice
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.llm.models.text.CompletionResult
import com.xebia.functional.xef.llm.models.usage.Usage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class OpenAIModel(
  private val openAI: OpenAI,
  override val name: String,
  override val modelType: ModelType
) : Chat, ChatWithFunctions, Images, Completion, Embeddings, AutoCloseable {

  private val client =
    OpenAIClient(
      host = openAI.getHost()?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
      token = openAI.getToken(),
      logging = LoggingConfig(LogLevel.None),
      headers = mapOf("Authorization" to " Bearer $openAI.token")
    )

  @OptIn(LegacyOpenAI::class)
  override suspend fun createCompletion(request: CompletionRequest): CompletionResult {
    fun completionChoice(it: OpenAIChoice): CompletionChoice =
      CompletionChoice(it.text, it.index, null, it.finishReason.value)

    val response = client.completion(toCompletionRequest(request))
    return CompletionResult(
      id = response.id,
      `object` = response.model.id,
      created = response.created,
      model = response.model.id,
      choices = response.choices.map { completionChoice(it) },
      usage = usage(response.usage)
    )
  }

  override suspend fun createChatCompletion(
    request: ChatCompletionRequest
  ): ChatCompletionResponse {
    fun chatMessage(cm: ChatMessage) =
      Message(
        role = toRole(cm.role),
        content = cm.content ?: "",
        name = cm.name ?: "",
      )

    fun toChoice(choice: ChatChoice): Choice =
      Choice(
        message = chatMessage(choice.message),
        finishReason = choice.finishReason.value,
        index = choice.index,
      )

    val response = client.chatCompletion(toChatCompletionRequest(request))
    return ChatCompletionResponse(
      id = response.id,
      `object` = response.model.id,
      created = response.created,
      model = response.model.id,
      choices = response.choices.map { toChoice(it) },
      usage = usage(response.usage)
    )
  }

  override suspend fun createChatCompletions(
    request: ChatCompletionRequest
  ): Flow<ChatCompletionChunk> {
    fun chatDelta(delta: OpenAIChatDelta): ChatDelta =
      ChatDelta(
        role = toRole(delta.role),
        content = delta.content,
        functionCall = delta.functionCall?.let { FnCall(it.name, it.arguments) }
      )

    fun chatChunk(chunk: OpenAIChatChunk): ChatChunk =
      ChatChunk(chunk.index, chatDelta(chunk.delta), chunk.finishReason?.value)

    fun chatCompletionChunk(response: OpenAIChatCompletionChunk): ChatCompletionChunk =
      ChatCompletionChunk(
        id = response.id,
        created = response.created,
        model = response.model.id,
        choices = response.choices.map { chatChunk(it) },
        usage = usage(response.usage)
      )

    return client.chatCompletions(toChatCompletionRequest(request)).map { chatCompletionChunk(it) }
  }

  override suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequest
  ): ChatCompletionResponseWithFunctions {
    fun toOpenAI(cf: CFunction): ChatCompletionFunction =
      ChatCompletionFunction(
        name = cf.name,
        description = cf.description,
        parameters = Parameters(Json.parseToJsonElement(cf.parameters)),
      )

    fun toOpenAIChatMessage(it: Message) =
      ChatMessage(role = fromRole(it.role), content = it.content, name = it.name)

    val openAIRequest: OpenAIChatCompletionRequest = chatCompletionRequest {
      model = ModelId(request.model)
      messages = request.messages.map { toOpenAIChatMessage(it) }
      functions = request.functions.map { toOpenAI(it) }
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
        functionCall = it.functionCall?.let { FnCall(it.name, it.arguments) }
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
        usage = usage(response.usage)
      )

    return fromResponse(client.chatCompletion(openAIRequest))
  }

  override suspend fun createEmbeddings(request: EmbeddingRequest): EmbeddingResult {
    val clientRequest: OpenAIEmbeddingRequest = embeddingRequest {
      model = ModelId(request.model)
      input = request.input
      user = request.user
    }

    fun createEmbedding(it: OpenAIEmbedding): Embedding =
      Embedding(it.embedding.map { it.toFloat() })

    val response = client.embeddings(clientRequest)
    return EmbeddingResult(
      data = response.embeddings.map { createEmbedding(it) },
      usage = usage(response.usage)
    )
  }

  @OptIn(BetaOpenAI::class)
  override suspend fun createImages(request: ImagesGenerationRequest): ImagesGenerationResponse {
    val clientRequest: ImageCreation = imageCreation {
      prompt = request.prompt.messages.firstOrNull()?.content
      n = request.numberImages
      size = ImageSize(request.size)
      user = request.user
    }

    val response = client.imageURL(clientRequest)
    return ImagesGenerationResponse(data = response.map { ImageGenerationUrl(it.url) })
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

  private fun usage(usage: OpenAIUsage?): Usage =
    Usage(
      promptTokens = usage?.promptTokens,
      completionTokens = usage?.completionTokens,
      totalTokens = usage?.totalTokens,
    )

  private fun toRole(it: ChatRole?) =
    when (it) {
      ChatRole.User -> Role.USER
      ChatRole.Assistant -> Role.ASSISTANT
      ChatRole.System -> Role.SYSTEM
      ChatRole.Function -> Role.SYSTEM
      else -> Role.ASSISTANT
    }

  private fun fromRole(it: Role) =
    when (it) {
      Role.USER -> ChatRole.User
      Role.ASSISTANT -> ChatRole.Assistant
      Role.SYSTEM -> ChatRole.System
    }

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
      if (request.functions.isNotEmpty())
        functions =
          request.functions.map {
            ChatCompletionFunction(
              name = it.name,
              description = it.description,
              parameters = Parameters(Json.parseToJsonElement(it.parameters)),
            )
          }
      if (request.functionCall != null)
        functionCall =
          request.functionCall?.get("name")?.let { FunctionMode.Named(it) } ?: FunctionMode.Auto
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
