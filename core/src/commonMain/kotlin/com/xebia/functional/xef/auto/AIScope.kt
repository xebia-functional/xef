package com.xebia.functional.xef.auto

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.tokenizer.Encoding
import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.tokenizer.truncateText
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.embeddings.Embeddings
import com.xebia.functional.xef.llm.AIClient
import com.xebia.functional.xef.llm.LLMModel
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequestWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.chat.Role
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.functions.FunctionCall
import com.xebia.functional.xef.llm.models.images.ImagesGenerationRequest
import com.xebia.functional.xef.llm.models.images.ImagesGenerationResponse
import com.xebia.functional.xef.llm.models.text.CompletionRequest
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.CombinedVectorStore
import com.xebia.functional.xef.vectorstores.LocalVectorStore
import com.xebia.functional.xef.vectorstores.VectorStore
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.jvm.JvmName

/**
 * The [AIScope] is the context in which [AI] values are run. It encapsulates all the dependencies
 * required to run [AI] values, and provides convenient syntax for writing [AI] based programs.
 */
class AIScope(
  val defaultModel: LLMModel,
  val defaultSerializationModel: LLMModel,
  val AIClient: AIClient,
  val context: VectorStore,
  val embeddings: Embeddings,
  val maxDeserializationAttempts: Int = 3,
  val user: String = "user",
  val echo: Boolean = false,
  val temperature: Double = 0.4,
  val numberOfPredictions: Int = 1,
  val docsInContext: Int = 20,
  val minResponseTokens: Int = 500,
  val logger: KLogger = KotlinLogging.logger {},
) {

  /**
   * Allows invoking [AI] values in the context of this [AIScope].
   *
   * ```kotlin
   * data class CovidNews(val title: String, val content: String)
   * val covidNewsToday = ai {
   *   val now = LocalDateTime.now()
   *   agent(search("$now covid-19 News")) {
   *     prompt<CovidNews>("write a paragraph of about 300 words about the latest news on covid-19 on $now")
   *   }
   * }
   *
   * data class BreakingNews(val title: String, val content: String, val date: String)
   *
   * fun breakingNews(date: LocalDateTime): AI<BreakingNews> = ai {
   *   agent(search("$date Breaking News")) {
   *     prompt("Summarize all breaking news that happened on ${now.minusDays(it)} in about 300 words")
   *   }
   * }
   *
   * suspend fun AIScope.breakingNewsLastWeek(): List<BreakingNews> {
   *   val now = LocalDateTime.now()
   *   return (0..7).parMap { breakingNews(now.minusDays(it)).invoke() }
   * }
   *
   * fun news(): AI<List<News>> = ai {
   *   val covidNews = parZip(
   *     { covidNewsToday() },
   *     { breakingNewsLastWeek() }
   *   ) { covidNews, breakingNews -> listOf(covidNews) + breakingNews }
   * }
   * ```
   */
  @AiDsl @JvmName("invokeAI") suspend operator fun <A> AI<A>.invoke(): A = invoke(this@AIScope)

  @AiDsl
  suspend fun extendContext(vararg docs: String) {
    context.addTexts(docs.toList())
  }

  /**
   * Creates a nested scope that combines the provided [store] with the outer _store_. This is done
   * using [CombinedVectorStore].
   *
   * **Note:** if the implementation of [VectorStore] is relying on resources you're manually
   * responsible for closing any potential resources.
   */
  @AiDsl
  suspend fun <A> contextScope(store: VectorStore, block: AI<A>): A =
    AIScope(
        defaultModel,
        defaultSerializationModel,
        this@AIScope.AIClient,
        CombinedVectorStore(store, this@AIScope.context),
        this@AIScope.embeddings,
      )
      .block()

  @AiDsl
  suspend fun <A> contextScope(block: AI<A>): A = contextScope(LocalVectorStore(embeddings), block)

  /** Add new [docs] to the [context], and then executes the [block]. */
  @AiDsl
  @JvmName("contextScopeWithDocs")
  suspend fun <A> contextScope(docs: List<String>, block: AI<A>): A = contextScope {
    extendContext(*docs.toTypedArray())
    block(this)
  }

  @AiDsl
  @JvmName("promptWithSerializer")
  suspend fun <A> prompt(
    prompt: Prompt,
    functions: List<CFunction>,
    serializer: (json: String) -> A,
    maxDeserializationAttempts: Int = this.maxDeserializationAttempts,
    model: LLMModel = defaultSerializationModel,
    user: String = this.user,
    echo: Boolean = this.echo,
    numberOfPredictions: Int = this.numberOfPredictions,
    temperature: Double = this.temperature,
    bringFromContext: Int = this.docsInContext,
    minResponseTokens: Int = this.minResponseTokens,
  ): A {
    return tryDeserialize(serializer, maxDeserializationAttempts) {
      promptMessage(
        prompt = prompt,
        model = model,
        functions = functions,
        user = user,
        echo = echo,
        numberOfPredictions = numberOfPredictions,
        temperature = temperature,
        bringFromContext = bringFromContext,
        minResponseTokens = minResponseTokens
      )
    }
  }

  suspend fun <A> AIScope.tryDeserialize(
    serializer: (json: String) -> A,
    maxDeserializationAttempts: Int,
    agent: AI<List<String>>
  ): A {
    val logger = KotlinLogging.logger {}
    (0 until maxDeserializationAttempts).forEach { currentAttempts ->
      val result = agent().firstOrNull() ?: throw AIError.NoResponse()
      catch({
        return@tryDeserialize serializer(result)
      }) { e: Throwable ->
        logger.error(e) { "Error deserializing response: $result\n${e.message}" }
        if (currentAttempts == maxDeserializationAttempts)
          throw AIError.JsonParsing(result, maxDeserializationAttempts, e.nonFatalOrThrow())
        // TODO else log attempt ?
      }
    }
    throw AIError.NoResponse()
  }

  @AiDsl
  suspend fun promptMessage(
    question: String,
    model: LLMModel = defaultModel,
    functions: List<CFunction> = emptyList(),
    user: String = this.user,
    echo: Boolean = this.echo,
    n: Int = this.numberOfPredictions,
    temperature: Double = this.temperature,
    bringFromContext: Int = this.docsInContext,
    minResponseTokens: Int = this.minResponseTokens
  ): List<String> =
    promptMessage(
      Prompt(question),
      model,
      functions,
      user,
      echo,
      n,
      temperature,
      bringFromContext,
      minResponseTokens
    )

  @AiDsl
  suspend fun promptMessage(
    prompt: Prompt,
    model: LLMModel = defaultModel,
    functions: List<CFunction> = emptyList(),
    user: String = this.user,
    echo: Boolean = this.echo,
    numberOfPredictions: Int = this.numberOfPredictions,
    temperature: Double = this.temperature,
    bringFromContext: Int = this.docsInContext,
    minResponseTokens: Int
  ): List<String> {
    return when (model.kind) {
      LLMModel.Kind.Completion ->
        callCompletionEndpoint(
          prompt.message,
          model,
          user,
          echo,
          numberOfPredictions,
          temperature,
          bringFromContext,
          minResponseTokens
        )
      LLMModel.Kind.Chat ->
        callChatEndpoint(
          prompt.message,
          model,
          user,
          numberOfPredictions,
          temperature,
          bringFromContext,
          minResponseTokens
        )
      LLMModel.Kind.ChatWithFunctions ->
        callChatEndpointWithFunctionsSupport(
            prompt.message,
            model,
            functions,
            user,
            numberOfPredictions,
            temperature,
            bringFromContext,
            minResponseTokens
          )
          .mapNotNull { it.arguments }
    }
  }

  private suspend fun callCompletionEndpoint(
    prompt: String,
    model: LLMModel,
    user: String,
    echo: Boolean,
    n: Int,
    temperature: Double,
    bringFromContext: Int,
    minResponseTokens: Int
  ): List<String> {
    val promptWithContext: String =
      promptWithContext(prompt, bringFromContext, model.modelType, minResponseTokens)

    val maxTokens: Int = checkTotalLeftTokens(model.modelType, "", promptWithContext)

    val request =
      CompletionRequest(
        model = model.name,
        user = user,
        prompt = promptWithContext,
        echo = echo,
        n = n,
        temperature = temperature,
        maxTokens = maxTokens
      )
    return AIClient.createCompletion(request).choices.map { it.text }
  }

  private suspend fun callChatEndpoint(
    prompt: String,
    model: LLMModel,
    user: String,
    n: Int,
    temperature: Double,
    bringFromContext: Int,
    minResponseTokens: Int
  ): List<String> {
    val role: String = Role.system.name
    val promptWithContext: String =
      promptWithContext(prompt, bringFromContext, model.modelType, minResponseTokens)
    val messages: List<Message> = listOf(Message(role, promptWithContext))
    val maxTokens: Int = checkTotalLeftChatTokens(messages, model)
    val request =
      ChatCompletionRequest(
        model = model.name,
        user = user,
        messages = messages,
        n = n,
        temperature = temperature,
        maxTokens = maxTokens
      )
    return AIClient.createChatCompletion(request).choices.mapNotNull { it.message?.content }
  }

  private suspend fun callChatEndpointWithFunctionsSupport(
    prompt: String,
    model: LLMModel,
    functions: List<CFunction>,
    user: String,
    n: Int,
    temperature: Double,
    bringFromContext: Int,
    minResponseTokens: Int
  ): List<FunctionCall> {
    val role: String = Role.user.name
    val firstFnName: String? = functions.firstOrNull()?.name
    val promptWithContext: String =
      promptWithContext(prompt, bringFromContext, model.modelType, minResponseTokens)
    val messages: List<Message> = listOf(Message(role, promptWithContext))
    val maxTokens: Int = checkTotalLeftChatTokens(messages, model)
    val request =
      ChatCompletionRequestWithFunctions(
        model = model.name,
        user = user,
        messages = messages,
        n = n,
        temperature = temperature,
        maxTokens = maxTokens,
        functions = functions,
        functionCall = mapOf("name" to (firstFnName ?: ""))
      )
    return AIClient.createChatCompletionWithFunctions(request).choices.mapNotNull {
      it.message?.functionCall
    }
  }

  private suspend fun promptWithContext(
    prompt: String,
    bringFromContext: Int,
    modelType: ModelType,
    minResponseTokens: Int
  ): String {
    val ctxInfo: List<String> = context.similaritySearch(prompt, bringFromContext)
    return createPromptWithContextAwareOfTokens(
      ctxInfo = ctxInfo,
      modelType = modelType,
      prompt = prompt,
      minResponseTokens = minResponseTokens
    )
  }

  private fun createPromptWithContextAwareOfTokens(
    ctxInfo: List<String>,
    modelType: ModelType,
    prompt: String,
    minResponseTokens: Int,
  ): String {
    val maxContextLength: Int = modelType.maxContextLength
    val promptTokens: Int = modelType.encoding.countTokens(prompt)
    val remainingTokens: Int = maxContextLength - promptTokens - minResponseTokens

    return if (ctxInfo.isNotEmpty() && remainingTokens > minResponseTokens) {
      val ctx: String = ctxInfo.joinToString("\n")

      if (promptTokens >= maxContextLength) {
        throw AIError.PromptExceedsMaxTokenLength(prompt, promptTokens, maxContextLength)
      }
      // truncate the context if it's too long based on the max tokens calculated considering the
      // existing prompt tokens
      // alternatively we could summarize the context, but that's not implemented yet
      val ctxTruncated: String = modelType.encoding.truncateText(ctx, remainingTokens)

      """|```Context
         |${ctxTruncated}
         |```
         |The context is related to the question try to answer the `goal` as best as you can
         |or provide information about the found content
         |```goal
         |${prompt}
         |```
         |ANSWER:
         |"""
        .trimMargin()
    } else prompt
  }

  private fun checkTotalLeftTokens(
    modelType: ModelType,
    role: String,
    promptWithContext: String
  ): Int =
    with(modelType) {
      val roleTokens: Int = encoding.countTokens(role)
      val padding = 20 // reserve 20 tokens for additional symbols around the context
      val promptTokens: Int = encoding.countTokens(promptWithContext)
      val takenTokens: Int = roleTokens + promptTokens + padding
      val totalLeftTokens: Int = maxContextLength - takenTokens
      if (totalLeftTokens < 0) {
        throw AIError.PromptExceedsMaxTokenLength(promptWithContext, takenTokens, maxContextLength)
      }
      logger.debug {
        "Tokens -- used: $takenTokens, model max: $maxContextLength, left: $totalLeftTokens"
      }
      totalLeftTokens
    }

  private fun checkTotalLeftChatTokens(messages: List<Message>, model: LLMModel): Int {
    val maxContextLength: Int = model.modelType.maxContextLength
    val messagesTokens: Int = tokensFromMessages(messages, model)
    val totalLeftTokens: Int = maxContextLength - messagesTokens
    if (totalLeftTokens < 0) {
      throw AIError.MessagesExceedMaxTokenLength(messages, messagesTokens, maxContextLength)
    }
    logger.debug {
      "Tokens -- used: $messagesTokens, model max: $maxContextLength, left: $totalLeftTokens"
    }
    return totalLeftTokens
  }

  private fun tokensFromMessages(messages: List<Message>, model: LLMModel): Int =
    when (model) {
      LLMModel.GPT_3_5_TURBO_FUNCTIONS -> {
        val paddingTokens = 200 // reserved for functions
        val fallbackModel: LLMModel = LLMModel.GPT_3_5_TURBO_0301
        logger.debug {
          "Warning: ${model.name} may change over time. " +
            "Returning messages num tokens assuming ${fallbackModel.name} + $paddingTokens padding tokens."
        }
        tokensFromMessages(messages, fallbackModel) + paddingTokens
      }
      LLMModel.GPT_3_5_TURBO -> {
        val paddingTokens = 5 // otherwise if the model changes, it might later fail
        val fallbackModel: LLMModel = LLMModel.GPT_3_5_TURBO_0301
        logger.debug {
          "Warning: ${model.name} may change over time. " +
            "Returning messages num tokens assuming ${fallbackModel.name} + $paddingTokens padding tokens."
        }
        tokensFromMessages(messages, fallbackModel) + paddingTokens
      }
      LLMModel.GPT_4,
      LLMModel.GPT_4_32K -> {
        val paddingTokens = 5 // otherwise if the model changes, it might later fail
        val fallbackModel: LLMModel = LLMModel.GPT_4_0314
        logger.debug {
          "Warning: ${model.name} may change over time. " +
            "Returning messages num tokens assuming ${fallbackModel.name} + $paddingTokens padding tokens."
        }
        tokensFromMessages(messages, fallbackModel) + paddingTokens
      }
      LLMModel.GPT_3_5_TURBO_0301 ->
        model.modelType.encoding.countTokensFromMessages(
          messages,
          tokensPerMessage = 4,
          tokensPerName = 0
        )
      LLMModel.GPT_4_0314 ->
        model.modelType.encoding.countTokensFromMessages(
          messages,
          tokensPerMessage = 3,
          tokensPerName = 2
        )
      else -> {
        val paddingTokens = 20
        val fallbackModel: LLMModel = LLMModel.GPT_3_5_TURBO_0301
        logger.debug {
          "Warning: calculation of tokens is partially supported for ${model.name} . " +
            "Returning messages num tokens assuming ${fallbackModel.name} + $paddingTokens padding tokens."
        }
        tokensFromMessages(messages, fallbackModel) + paddingTokens
      }
    }

  private fun Encoding.countTokensFromMessages(
    messages: List<Message>,
    tokensPerMessage: Int,
    tokensPerName: Int
  ): Int =
    messages.sumOf { message ->
      countTokens(message.role) +
        countTokens(message.content ?: "") +
        tokensPerMessage +
        (message.name?.let { tokensPerName } ?: 0)
    } + 3

  /**
   * Run a [prompt] describes the images you want to generate within the context of [AIScope].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [Prompt] describing the images you want to generate.
   * @param numberImages number of images to generate.
   * @param size the size of the images to generate.
   */
  suspend fun images(
    prompt: String,
    user: String = "testing",
    numberImages: Int = 1,
    size: String = "1024x1024",
    bringFromContext: Int = 10
  ): ImagesGenerationResponse = images(Prompt(prompt), user, numberImages, size, bringFromContext)

  /**
   * Run a [prompt] describes the images you want to generate within the context of [AIScope].
   * Returns a [ImagesGenerationResponse] containing time and urls with images generated.
   *
   * @param prompt a [Prompt] describing the images you want to generate.
   * @param numberImages number of images to generate.
   * @param size the size of the images to generate.
   */
  suspend fun images(
    prompt: Prompt,
    user: String = "testing",
    numberImages: Int = 1,
    size: String = "1024x1024",
    bringFromContext: Int = 10
  ): ImagesGenerationResponse {
    val ctxInfo = context.similaritySearch(prompt.message, bringFromContext)
    val promptWithContext =
      if (ctxInfo.isNotEmpty()) {
        """|Instructions: Use the [Information] below delimited by 3 backticks to accomplish
         |the [Objective] at the end of the prompt.
         |Try to match the data returned in the [Objective] with this [Information] as best as you can.
         |[Information]:
         |```
         |${ctxInfo.joinToString("\n")}
         |```
         |$prompt"""
          .trimMargin()
      } else prompt.message
    val request =
      ImagesGenerationRequest(
        prompt = promptWithContext,
        numberImages = numberImages,
        size = size,
        user = user
      )
    return AIClient.createImages(request)
  }
}
