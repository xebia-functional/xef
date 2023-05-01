package com.xebia.functional.auto

import arrow.core.getOrElse
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.auto.agents.Agent
import com.xebia.functional.auto.model.Task
import com.xebia.functional.auto.serialization.buildJsonSchema
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.*
import com.xebia.functional.vectorstores.LocalVectorStore
import com.xebia.functional.vectorstores.VectorStore
import io.github.oshai.KotlinLogging
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import kotlin.time.ExperimentalTime

@PublishedApi
internal val logger = KotlinLogging.logger("AutoAI")

val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
}

@OptIn(ExperimentalTime::class)
suspend inline fun <reified A> ai(
  prompt: String,
  engine: HttpClientEngine? = null,
  agents: List<Agent<*>> = emptyList()
): A {
  val descriptor = serialDescriptor<A>()
  val jsonSchema = buildJsonSchema(descriptor)
  return resourceScope {
    either {
      val openAIConfig = OpenAIConfig()
      val openAiClient = KtorOpenAIClient(openAIConfig, engine)
      val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
      val vectorStore = LocalVectorStore(embeddings)
      ai(prompt, descriptor, serializer<A>(), jsonSchema, openAiClient, vectorStore, agents)
    }.getOrElse { throw IllegalStateException(it.joinToString()) }
  }
}

suspend fun <A> ai(
  prompt: String,
  descriptor: SerialDescriptor,
  deserializationStrategy: DeserializationStrategy<A>,
  jsonSchema: JsonObject,
  openAIClient: OpenAIClient,
  vectorStore: VectorStore,
  agents: List<Agent<*>> = emptyList()
): A {
  val augmentedPrompt = """
                |You are an AI who performs one task based on the following objective: 
                |Objective: $prompt
                |Instructions: Only communicate back to the user in valid JSON.
                |IMPORTANT: Your message must be valid JSON and always finishes after the json object is closed.
                |Use exclusively the following JSON schema to produce the result in JSON format.
                |Ensure that your output conforms to the JSON schema specified below.
                |number is a number without delimiters
                |boolean is true or false
                |strings should be properly escaped to ensure json remains readable
                |JSON Schema:$jsonSchema
            """.trimMargin()
  val result = if (agents.isNotEmpty()) {
    val resolutionContext = solveObjective(Task(-1, prompt, emptyList()), agents = agents).result
    val promptWithContext = """
                    |$resolutionContext
                    |
                    |Given this information solve the objective:
                    |
                    |$augmentedPrompt
                """.trimMargin()
    openAIChatCall(openAIClient, promptWithContext)
  } else {
    openAIChatCall(openAIClient, augmentedPrompt)
  }
  return catch({
    json.decodeFromString(deserializationStrategy, result)
  }) { e ->
    val fixJsonPrompt = """
                    |RESULT: 
                    |$result
                    |Exception: 
                    |${e.message}
                    |${e.stackTraceToString()}
                    |Objective: $prompt
                    |Instructions: Use the following JSON schema to produce the result on valid json format avoiding the exception
                    |JSON Schema:$jsonSchema
                    """.trimMargin()
    logger.debug { "Attempting to Fix JSON due to error: ${e.message}" }
    ai(fixJsonPrompt, descriptor, deserializationStrategy, jsonSchema, openAIClient, vectorStore)
      .also { logger.debug { "Fixed JSON: $it" } }
  }
}

private suspend fun openAIChatCall(
  openAIClient: OpenAIClient,
  promptWithContext: String
): String {
  val res = chatCompletionResponse(openAIClient, promptWithContext, "gpt-3.5-turbo", "AI_Value_Generator")
  val msg = res.choices.firstOrNull()?.message?.content
  requireNotNull(msg) { "No message found in result: $res" }
  return msg
}

private suspend fun chatCompletionResponse(openAIClient: OpenAIClient, prompt: String, model: String, user: String): ChatCompletionResponse {
  val completionRequest = ChatCompletionRequest(
    model = model,
    messages = listOf(Message(Role.system.name, prompt, user)),
    user = user
  )
  return openAIClient.createChatCompletion(completionRequest)
}
