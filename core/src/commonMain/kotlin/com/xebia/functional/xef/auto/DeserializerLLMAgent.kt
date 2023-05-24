@file:JvmMultifileClass
@file:JvmName("Agent")
package com.xebia.functional.xef.auto

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import arrow.core.raise.ensureNotNull
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.serialization.buildJsonSchema
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.append
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
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
@JvmName("promptWithSerializer")
suspend fun <A> AIScope.prompt(
  prompt: Prompt,
  descriptor: SerialDescriptor,
  serializer: (json: String) -> A,
  maxDeserializationAttempts: Int = 5,
  model: LLMModel = LLMModel.GPT_3_5_TURBO,
  user: String = "testing",
  echo: Boolean = false,
  n: Int = 1,
  temperature: Double = 0.0,
  bringFromContext: Int = 10,
  minResponseTokens: Int = 500,
): A {
  val jsonSchema = buildJsonSchema(descriptor, false)
  val responseInstructions =
    """
        |
        |Response Instructions: 
        |1. Return the entire response in a single line with not additional lines or characters.
        |2. When returning the response consider <string> values should be accordingly escaped so the json remains valid.
        |3. Use the JSON schema to produce the result exclusively in valid JSON format.
        |4. Pay attention to required vs non-required fields in the schema.
        |JSON Schema:
        |$jsonSchema
        |Response:
        """
      .trimMargin()

  return tryDeserialize(serializer, maxDeserializationAttempts) {
    promptMessage(
      prompt.append(responseInstructions),
      model,
      user,
      echo,
      n,
      temperature,
      bringFromContext,
      minResponseTokens
    )
  }
}

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
  bringFromContext: Int = 10,
  minResponseTokens: Int = 500,
): A = prompt(
  prompt,
  serializer.descriptor,
  { json.decodeFromString(serializer, it) },
  maxDeserializationAttempts, model, user, echo, n, temperature, bringFromContext, minResponseTokens
)

suspend fun <A> AIScope.tryDeserialize(
  serializer: (json: String) -> A,
  maxDeserializationAttempts: Int,
  agent: AI<List<String>>
): A {
  var currentAttempts = 0
  while (currentAttempts < maxDeserializationAttempts) {
    currentAttempts++
    val result = ensureNotNull(agent().firstOrNull()) { AIError.NoResponse }
    catch({
      return@tryDeserialize serializer(result)
    }) { e: Throwable ->
      if (currentAttempts == maxDeserializationAttempts)
        raise(AIError.JsonParsing(result, maxDeserializationAttempts, e.nonFatalOrThrow()))
      // else continue with the next attempt
    }
  }
  raise(AIError.NoResponse)
}

