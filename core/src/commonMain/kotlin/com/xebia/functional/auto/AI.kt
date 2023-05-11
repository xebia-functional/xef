package com.xebia.functional.auto

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.recover
import arrow.core.right
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.AIError
import com.xebia.functional.auto.serialization.buildJsonSchema
import com.xebia.functional.auto.serialization.sample
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.*
import com.xebia.functional.logTruncated
import com.xebia.functional.persistence.LocalPersistence
import com.xebia.functional.persistence.Persistence
import com.xebia.functional.chains.Agent
import com.xebia.functional.chains.storeResults
import com.xebia.functional.persistence.LocalPersistenceSimilarity
import com.xebia.functional.persistence.PersistenceSimilarity
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import kotlin.jvm.JvmName
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import kotlin.time.ExperimentalTime

@DslMarker annotation class AiDsl

data class SerializationConfig<A>(
  val jsonSchema: JsonObject,
  val descriptor: SerialDescriptor,
  val deserializationStrategy: DeserializationStrategy<A>,
)

/**
 * An [AI] value represents an action relying on artificial intelligence that can be run to produce
 * an [A]. This value is _lazy_ and can be combined with other `AI` values using [AIScope.invoke],
 * and thus forms a monadic DSL.
 *
 * All [AI] actions that are composed together using [AIScope.invoke] share the same [Persistence],
 * [OpenAIEmbeddings] and [OpenAIClient] instances.
 */
typealias AI<M, A> = suspend AIScope<M>.() -> A

/** A DSL block that makes it more convenient to construct [AI] values. */
inline fun <A> ai(noinline block: suspend AIScope<String>.() -> A): AI<String, A> = block

/** A DSL block that makes it more convenient to construct [AI] values. */
inline fun <M, A> aiWithMemory(noinline block: suspend AIScope<M>.() -> A): AI<M, A> = block

/**
 * Applies a transformation to the result of [AI].
 */
inline fun <M, A, B> AI<M, A>.map(crossinline transform: (A) -> B): AI<M, B> =
  aiWithMemory { transform(this@map(this)) }

/**
 * Run the [AI] value to produce an [A], this method initialises all the dependencies required to
 * run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 */
suspend inline fun <M, reified A> AI<M, A>.getOrElse(crossinline orElse: suspend (AIError) -> A): A =
  recover({
    resourceScope {
      val openAIConfig = OpenAIConfig()
      val openAiClient: OpenAIClient = KtorOpenAIClient(openAIConfig)
      val logger = KotlinLogging.logger("AutoAI")
      // val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
      val persistence = LocalPersistence<M>()
      val scope = AIScope(openAiClient, persistence, logger, this, this@recover)
      invoke(scope)
    }
  }) {
    orElse(it)
  }

/**
 * Run the [AI] value to produce an [A], this method initialises all the dependencies required to
 * run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 */
@OptIn(ExperimentalTime::class)
suspend inline fun <reified A> AI<String, A>.getOrElseSimilarity(crossinline orElse: suspend (AIError) -> A): A =
  recover({
    resourceScope {
      val openAIConfig = OpenAIConfig()
      val openAiClient: OpenAIClient = KtorOpenAIClient(openAIConfig)
      val logger = KotlinLogging.logger("AutoAI")
      val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
      val persistence = LocalPersistenceSimilarity(embeddings)
      val scope = AIScope(openAiClient, persistence, logger, this, this@recover)
      invoke(scope)
    }
  }) {
    orElse(it)
  }

/**
 * Run the [AI] value to produce _either_ an [AIError], or [A]. this method initialises all the
 * dependencies required to run the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 *
 * @see getOrElse for an operator that allow directly handling the [AIError] case.
 */
suspend inline fun <M, reified A> AI<M, A>.toEither(): Either<AIError, A> =
  this.map { it.right() }.getOrElse { it.left() }

// TODO: Allow traced transformation of Raise errors
class AIException(message: String) : RuntimeException(message)

/**
 * Run the [AI] value to produce [A]. this method initialises all the dependencies required to run
 * the [AI] value and once it finishes it closes all the resources.
 *
 * This operator is **terminal** meaning it runs and completes the _chain_ of `AI` actions.
 *
 * @throws AIException in case something went wrong.
 * @see getOrElse for an operator that allow directly handling the [AIError] case instead of
 *   throwing.
 */
suspend inline fun <M, reified A> AI<M, A>.getOrThrow(): A = getOrElse { throw AIException(it.reason) }

/**
 * The [AIScope] is the context in which [AI] values are run. It encapsulates all the dependencies
 * required to run [AI] values, and provides convenient syntax for writing [AI] based programs.
 *
 * It exposes the [ResourceScope] so you can easily add your own resources with the scope of the
 * [AI] program, and [Raise] of [AIError] in case you want to compose any [Raise] based actions.
 */
class AIScope<M>(
  private val openAIClient: OpenAIClient,
  private val persistence: Persistence<M, *>,
  private val logger: KLogger,
  resourceScope: ResourceScope,
  raise: Raise<AIError>,
  private val json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
) : ResourceScope by resourceScope, Raise<AIError> by raise {

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
  @AiDsl @JvmName("invokeAI") suspend operator fun <A> AI<M, A>.invoke(): A = invoke(this@AIScope)

  /** Creates a child scope of this [AIScope] with the specified [agent]. */
  @AiDsl
  suspend fun <A> agent(agent: Agent<M>, scope: suspend AIScope<M>.() -> A): A =
    agent(arrayOf(agent), scope)

  /** Creates a child scope of this [AIScope] with the specified [agents]. */
  @AiDsl
  suspend fun <A> agent(agents: Array<out Agent<M>>, scope: suspend AIScope<M>.() -> A): A {
    agents.storeResults(persistence)
    return scope(AIScope(openAIClient, persistence, logger, this, this))
  }

  /**
   * Run a [prompt] describes the task you want to solve within the context of [AIScope].
   * Returns a value of [A] where [A] **has to be** annotated with [kotlinx.serialization.Serializable].
   *
   * @throws SerializationException if serializer cannot be created (provided [A] or its type
   *   argument is not serializable).
   * @throws IllegalArgumentException if any of [A]'s type arguments contains star projection
   */
  @AiDsl
  suspend inline fun <reified A> prompt(prompt: String): A {
    val serializer = serializer<A>()
    val serializationConfig: SerializationConfig<A> =
      SerializationConfig(
        jsonSchema = buildJsonSchema(serializer.descriptor, false),
        descriptor = serializer.descriptor,
        deserializationStrategy = serializer
      )
    return prompt(prompt, serializationConfig)
  }

  @AiDsl
  suspend fun <A> prompt(
    prompt: String,
    serializationConfig: SerializationConfig<A>,
    maxAttempts: Int = 5,
    llmModel: LLMModel = LLMModel.GPT_3_5_TURBO,
  ): A {
    logger.logTruncated("AI", "Solving objective: $prompt")
    val result = openAIChatCall(prompt, serializationConfig, llmModel)
    logger.logTruncated("AI", "Response: $result")
    return catch({ json.decodeFromString(serializationConfig.deserializationStrategy, result) }) {
      e: IllegalArgumentException ->
      if (maxAttempts <= 0) raise(AIError.JsonParsing(result, maxAttempts, e))
      else {
        logger.logTruncated("System", "Error deserializing result, trying again... ${e.message}")
        prompt(prompt, serializationConfig, maxAttempts - 1).also {
          logger.debug { "Fixed JSON: $it" }
        }
      }
    }
  }

  private suspend fun openAIChatCall(
    prompt: String,
    serializationConfig: SerializationConfig<*>,
    llmModel: LLMModel,
  ): String {
    val context =
      kotlin.runCatching {
        (persistence as PersistenceSimilarity<String, *>).similaritySearch(prompt, 10)
      }.getOrElse { emptyList() }
    val promptWithMemory =
      if (context.isNotEmpty())
        """
                |Instructions: Use the [Information] below delimited by 3 backticks to accomplish
                |the [Objective] at the end of the prompt.
                |Try to match the data returned in the [Objective] with this [Information] as best as you can.
                |[Information]:
                |```
                |${context.joinToString("\n")}
                |```
                |$prompt
                """
          .trimMargin()
      else prompt
    val augmentedPrompt =
      """
                |$promptWithMemory
                |
                |Response Instructions: Use the following JSON schema to produce the result exclusively in valid JSON format
                |JSON Schema:
                |${serializationConfig.jsonSchema}
                |Response Example:
                |${serializationConfig.descriptor.sample()}
                |Response:
            """
        .trimMargin()
    val res = chatCompletionResponse(augmentedPrompt, llmModel.name, "AI_Value_Generator")
    val msg = res.choices.firstOrNull()?.message?.content
    requireNotNull(msg) { "No message found in result: $res" }
    logger.logTruncated("AI", "Response: $msg", 100)
    return msg
  }

  private suspend fun chatCompletionResponse(
    prompt: String,
    model: String,
    user: String
  ): ChatCompletionResponse {
    val completionRequest =
      ChatCompletionRequest(
        model = model,
        messages = listOf(Message(Role.system.name, prompt, user)),
        user = user
      )
    return openAIClient.createChatCompletion(completionRequest)
  }
}
