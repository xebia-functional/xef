package com.xebia.functional.auto

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.recover
import arrow.core.right
import arrow.fx.coroutines.Atomic
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.AIError
import com.xebia.functional.auto.serialization.buildJsonSchema
import com.xebia.functional.auto.serialization.sample
import com.xebia.functional.chains.Chain
import com.xebia.functional.chains.VectorQAChain
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.ChatCompletionRequest
import com.xebia.functional.llm.openai.ChatCompletionResponse
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.llm.openai.Message
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.llm.openai.Role
import com.xebia.functional.logTruncated
import com.xebia.functional.tools.Tool
import com.xebia.functional.vectorstores.LocalVectorStore
import com.xebia.functional.vectorstores.VectorStore
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import kotlin.jvm.JvmName
import kotlinx.serialization.serializer
import kotlin.time.ExperimentalTime
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@DslMarker
annotation class AIDSL

data class SerializationConfig<A>(
  val jsonSchema: JsonObject,
  val descriptor: SerialDescriptor,
  val deserializationStrategy: DeserializationStrategy<A>,
)

/*
 * With context receivers this can become more generic,
 * suspend context(Raise<AIError>, ResourceScope, AIContext) () -> A
 */
typealias AI<A> = suspend AIScope.() -> A

inline fun <A> prompt(noinline block: suspend AIScope.() -> A): AI<A> = block

@OptIn(ExperimentalTime::class)
suspend inline fun <reified A> AI<A>.getOrElse(crossinline orElse: suspend (AIError) -> A): A =
  resourceScope {
    recover({
      val openAIConfig = OpenAIConfig()
      val openAiClient: OpenAIClient = KtorOpenAIClient(openAIConfig)
      val logger = KotlinLogging.logger("AutoAI")
      val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
      val vectorStore = LocalVectorStore(embeddings)
      val scope = AIScope(openAiClient, vectorStore, Atomic(listOf()), logger, this@resourceScope, this)
      invoke(scope)
    }) { orElse(it) }
  }

suspend inline fun <reified A> AI<A>.toEither(): Either<AIError, A> =
  prompt { invoke().right() }.getOrElse { it.left() }

// TODO: Allow traced transformation of Raise errors
class AIException(message: String) : RuntimeException(message)

suspend inline fun <reified A> AI<A>.getOrThrow(): A =
  getOrElse { throw AIException(it.reason) }

class AIScope(
  private val openAIClient: OpenAIClient,
  private val vectorStore: VectorStore,
  private val agents: Atomic<List<Agent>>,
  private val logger: KLogger,
  resourceScope: ResourceScope,
  raise: Raise<AIError>,
  private val json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
) : ResourceScope by resourceScope, Raise<AIError> by raise {

  @AIDSL
  @JvmName("invokeAI")
  suspend operator fun <A> AI<A>.invoke(): A =
    invoke(this@AIScope)

  @AIDSL
  suspend fun <A> prompt(
    prompt: String,
    serializationConfig: SerializationConfig<A>,
    maxAttempts: Int = 5,
  ): A {
    logger.logTruncated("AI", "Solving objective: $prompt")
    val result = openAIChatCall(prompt, prompt, serializationConfig)
    logger.logTruncated("AI", "Response: $result")
    return catch({
      json.decodeFromString(serializationConfig.deserializationStrategy, result)
    }) { e: IllegalArgumentException ->
      if (maxAttempts <= 0) raise(AIError.JsonParsing(result, maxAttempts, e))
      else {
        logger.logTruncated("System", "Error deserializing result, trying again... ${e.message}")
        prompt(prompt, serializationConfig, maxAttempts - 1).also { logger.debug { "Fixed JSON: $it" } }
      }
    }
  }

  @AIDSL
  suspend fun <A> agent(tool: Tool, scope: suspend () -> A): A {
    val scopedAgent = Agent(listOf(tool))
    agents.update { it + scopedAgent }
    val result = scope()
    agents.update { it - scopedAgent }
    return result
  }

  @AIDSL
  suspend fun <A> agent(tool: Array<out Tool>, scope: suspend () -> A): A {
    val scopedAgent = Agent(tool)
    agents.update { it + scopedAgent }
    val result = scope()
    agents.update { it - scopedAgent }
    return result
  }

  @AIDSL
  suspend inline fun <reified A> prompt(prompt: String): A {
    val serializer = serializer<A>()
    val serializationConfig: SerializationConfig<A> = SerializationConfig(
      jsonSchema = buildJsonSchema(serializer.descriptor, false),
      descriptor = serializer.descriptor,
      deserializationStrategy = serializer
    )
    return prompt(prompt, serializationConfig)
  }

  private suspend fun Raise<AIError>.openAIChatCall(
    question: String,
    promptWithContext: String,
    serializationConfig: SerializationConfig<*>,
  ): String {
    //run the agents so they store context in the database
    agents.get().forEach { agent ->
      agent.storeResults(vectorStore)
    }
    //run the vectorQAChain to get the answer
    val numOfDocs = 10
    val outputVariable = "answer"
    val chain = VectorQAChain(
      openAIClient,
      vectorStore,
      numOfDocs,
      outputVariable
    )
    val contextQuestion = """|
            |Provide a solution as answer to the question or objective below:
            |```
            |$question
            |```
        """.trimMargin()
    val chainResults: Map<String, String> = chain.run(contextQuestion).bind()
    logger.debug { "Chain results: $chainResults" }
    val promptWithMemory =
      if (chainResults.isNotEmpty())
        """
                |Instructions: Use the [Information] below delimited by 3 backticks to accomplish
                |the [Objective] at the end of the prompt.
                |Try to match the data returned in the [Objective] with this [Information] as best as you can.
                |[Information]:
                |```
                |${chainResults.entries.joinToString("\n") { (k, v) -> "$k: $v" }}
                |```
                |$promptWithContext
                """.trimMargin()
      else promptWithContext
    val augmentedPrompt = """
                |$promptWithMemory
                |
                |Response Instructions: Use the following JSON schema to produce the result exclusively in valid JSON format
                |JSON Schema:
                |${serializationConfig.jsonSchema}
                |Response Example:
                |${serializationConfig.descriptor.sample()}
                |Response:
            """.trimMargin()
    val res = chatCompletionResponse(augmentedPrompt, "gpt-3.5-turbo", "AI_Value_Generator")
    val msg = res.choices.firstOrNull()?.message?.content
    requireNotNull(msg) { "No message found in result: $res" }
    logger.logTruncated("AI", "Response: $msg", 100)
    return msg
  }

  private suspend fun chatCompletionResponse(
    prompt: String, model: String, user: String
  ): ChatCompletionResponse {
    val completionRequest = ChatCompletionRequest(
      model = model, messages = listOf(Message(Role.system.name, prompt, user)), user = user
    )
    return openAIClient.createChatCompletion(completionRequest)
  }
}
