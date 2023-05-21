package com.xebia.functional.xef.auto

import arrow.core.raise.catch
import arrow.core.raise.ensureNotNull
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.serialization.buildJsonSchema
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.append
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

/**
 * Run a [question] describes the task you want to solve within the context of [AIScope]. Returns a
 * value of [A] where [A] **has to be** annotated with [kotlinx.serialization.Serializable].
 *
 * @throws SerializationException if serializer cannot be created (provided [A] or its type argument
 *   is not serializable).
 * @throws IllegalArgumentException if any of [A]'s type arguments contains star projection.
 */
@AiDsl
suspend inline fun <reified A> AIScope.prompt(
  question: String,
  json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  maxDeserializationAttempts: Int = 5,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10
): A =
  prompt(
    Prompt(question),
    json,
    maxDeserializationAttempts,
    model,
    user,
    echo,
    n,
    temperature,
    bringFromContext
  )

/**
 * Run a [prompt] describes the task you want to solve within the context of [AIScope]. Returns a
 * value of [A] where [A] **has to be** annotated with [kotlinx.serialization.Serializable].
 *
 * @throws SerializationException if serializer cannot be created (provided [A] or its type argument
 *   is not serializable).
 * @throws IllegalArgumentException if any of [A]'s type arguments contains star projection.
 */
@AiDsl
suspend inline fun <reified A> AIScope.prompt(
  prompt: Prompt,
  json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  maxDeserializationAttempts: Int = 5,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10
): A =
  prompt(
    prompt,
    serializer(),
    json,
    maxDeserializationAttempts,
    model,
    user,
    echo,
    n,
    temperature,
    bringFromContext
  )

@AiDsl
suspend fun <A> AIScope.prompt(
  prompt: Prompt,
  serializer: KSerializer<A>,
  json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  maxDeserializationAttempts: Int = 5,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10
): A {
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

  return tryDeserialize(serializationConfig, json, maxDeserializationAttempts) {
    promptMessage(
      prompt.append(responseInstructions),
      model,
      user,
      echo,
      n,
      temperature,
      bringFromContext
    )
  }
}

suspend fun <A> AIScope.tryDeserialize(
  serializationConfig: SerializationConfig<A>,
  json: Json,
  maxDeserializationAttempts: Int,
  agent: AI<List<String>>
): A {
  var currentAttempts = 0
  while (currentAttempts < maxDeserializationAttempts) {
    currentAttempts++
    val result = ensureNotNull(agent().firstOrNull()) { AIError.NoResponse }
    catch({
      return@tryDeserialize json.decodeFromString(serializationConfig.deserializationStrategy, result)
    }) {
      e: IllegalArgumentException ->
      if (currentAttempts == maxDeserializationAttempts)
        raise(AIError.JsonParsing(result, maxDeserializationAttempts, e))
      // else continue with the next attempt
    }
  }
  raise(AIError.NoResponse)
}
