package com.xebia.functional.auto

import arrow.core.getOrElse
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.llm.openai.OpenAIClient
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
): A {
  val descriptor = serialDescriptor<A>()
  val jsonSchema = buildJsonSchema(descriptor)
  return resourceScope {
    either {
      val openAIConfig = OpenAIConfig()
      val openAiClient = KtorOpenAIClient(openAIConfig, engine)
      val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
      val vectorStore = LocalVectorStore(embeddings)
      ai(prompt, descriptor, serializer<A>(), jsonSchema, openAiClient, vectorStore)
    }.getOrElse { throw IllegalStateException(it.joinToString()) }
  }
}

suspend fun <A> ai(
  prompt: String,
  descriptor: SerialDescriptor,
  deserializationStrategy: DeserializationStrategy<A>,
  jsonSchema: JsonObject,
  openAIClient: OpenAIClient,
  vectorStore: VectorStore
): A {
  val augmentedPrompt = """
                |Objective: $prompt
                |Instructions: Use the following JSON schema to produce the result on json format
                |JSON Schema:$jsonSchema
                |If you complete the objective return exactly the JSON response finished by the delimiter %COMPLETED%
                |If you can't complete the tasks do not return the JSON but instead information with the delimiter %FAILED%
            """.trimMargin()
  val result = AutoAI(
    LLM("gpt-3.5-turbo"),
    User("AI_Value_Generator"),
    openAIClient,
    vectorStore
  ).invoke(Objective(augmentedPrompt))
  require(result != null) { "No result found" }
  return catch({ json.decodeFromString(deserializationStrategy, result.value()) }) { e ->
    val fixJsonPrompt = """
                    |RESULT: 
                    |$result
                    |Exception: 
                    |${e.message}
                    |${e.printStackTrace()}
                    |Objective: $prompt
                    |Instructions: Use the following JSON schema to produce the result on valid json format avoiding the exception
                    |JSON Schema:$jsonSchema
                    |If you complete the objective return exactly the JSON response finished by the delimiter %COMPLETED%
                    |If you can't complete the tasks do not return the JSON but instead information with the delimiter %FAILED%
                    """.trimMargin()
    logger.debug { "Attempting to Fix JSON due to error: ${e.message}" }
    ai(fixJsonPrompt, descriptor, deserializationStrategy, jsonSchema, openAIClient, vectorStore)
      .also { logger.debug { "Fixed JSON: $it" } }
  }
}
