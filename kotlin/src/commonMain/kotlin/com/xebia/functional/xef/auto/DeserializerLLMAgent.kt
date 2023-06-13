package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.serialization.encodeJsonSchema
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.prompt.Prompt
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
  bringFromContext: Int = 10,
  minResponseTokens: Int = 500,
): A =
  prompt(
    prompt,
    encodeJsonSchema(serializer.descriptor),
    { json.decodeFromString(serializer, it) },
    maxDeserializationAttempts,
    model,
    user,
    echo,
    n,
    temperature,
    bringFromContext,
    minResponseTokens
  )
