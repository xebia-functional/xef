package com.xebia.functional.agents

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.ensureNotNull
import com.xebia.functional.AIError
import com.xebia.functional.auto.SerializationConfig
import com.xebia.functional.auto.serialization.buildJsonSchema
import com.xebia.functional.llm.openai.LLMModel
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate
import com.xebia.functional.prompt.append
import com.xebia.functional.vectorstores.VectorStore
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class DeserializerLLMAgent<A>(
  serializer: KSerializer<A>,
  private val json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  private val maxDeserializationAttempts: Int = 5,
  llm: OpenAIClient,
  template: PromptTemplate<String>,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  context: VectorStore = VectorStore.EMPTY,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10
) : Agent<Map<String, String>, A> {

  companion object {
    inline operator fun <reified A> invoke(
      llm: OpenAIClient,
      template: PromptTemplate<String>,
      model: LLMModel = LLMModel.GPT_3_5_TURBO,
      context: VectorStore = VectorStore.EMPTY,
      user: String = "testing",
      echo: Boolean = false,
      n: Int = 1,
      temperature: Double = 0.0,
      bringFromContext: Int = 10,
      json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
      },
      maxDeserializationAttempts: Int = 5,
    ): DeserializerLLMAgent<A> =
      DeserializerLLMAgent(
        serializer<A>(),
        json,
        maxDeserializationAttempts,
        llm,
        template,
        model,
        context,
        user,
        echo,
        n,
        temperature,
        bringFromContext
      )
  }

  val serializationConfig: SerializationConfig<A> =
    SerializationConfig(
      jsonSchema = buildJsonSchema(serializer.descriptor, false),
      descriptor = serializer.descriptor,
      deserializationStrategy = serializer
    )

  val responseInstructions =
    """
        |
        |Response Instructions: 
        |1. Return the entire response in a single line with not additional lines or characters.
        |2. When returning the response consider <string> values should be accordingly escaped so the json remains valid.
        |3. Use the JSON schema to produce the result exclusively in valid JSON format.
        |4. Pay attention to required vs non-required fields in the schema.
        |JSON Schema:
        |${serializationConfig.jsonSchema}
        |Response:
        """
      .trimMargin()

  val underlying: LLMAgent =
    LLMAgent(
      llm,
      template.append(responseInstructions),
      model,
      context,
      user,
      echo,
      n,
      temperature,
      bringFromContext
    )

  override val name: String = "Deserializer LLM Agent"
  override val description: String =
    "Runs a query through a LLM agent and deserializes the output from a JSON representation"

  override suspend fun Raise<AIError>.call(input: Map<String, String>): A {
    var currentAttempts = 0
    while (currentAttempts < maxDeserializationAttempts) {
      currentAttempts++
      val result =
        ensureNotNull(with(underlying) { call(input) }.firstOrNull()) { AIError.NoResponse }
      catch({
        return@call json.decodeFromString(serializationConfig.deserializationStrategy, result)
      }) { e: IllegalArgumentException ->
        if (currentAttempts == maxDeserializationAttempts)
          raise(AIError.JsonParsing(result, maxDeserializationAttempts, e))
        // else continue with the next attempt
      }
    }
    raise(AIError.NoResponse)
  }
}
