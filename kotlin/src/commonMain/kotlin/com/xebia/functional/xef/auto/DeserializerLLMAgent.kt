package com.xebia.functional.xef.auto

import com.xebia.functional.xef.auto.serialization.encodeJsonSchema
import com.xebia.functional.xef.llm.openai.LLMModel
import com.xebia.functional.xef.llm.openai.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import kotlinx.serialization.ExperimentalSerializationApi
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
  maxDeserializationAttempts: Int = this.maxDeserializationAttempts,
  model: LLMModel = this.defaultSerializationModel,
  user: String = this.user,
  echo: Boolean = this.echo,
  n: Int = this.numberOfPredictions,
  temperature: Double = this.temperature,
  bringFromContext: Int = this.docsInContext
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
  maxDeserializationAttempts: Int = this.maxDeserializationAttempts,
  model: LLMModel = this.defaultSerializationModel,
  user: String = this.user,
  echo: Boolean = this.echo,
  n: Int = this.numberOfPredictions,
  temperature: Double = this.temperature,
  bringFromContext: Int = this.docsInContext
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
  maxDeserializationAttempts: Int = this.maxDeserializationAttempts,
  model: LLMModel = this.defaultSerializationModel,
  user: String = "testing",
  echo: Boolean = this.echo,
  n: Int = this.numberOfPredictions,
  temperature: Double = this.temperature,
  bringFromContext: Int = this.docsInContext,
  minResponseTokens: Int = this.minResponseTokens,
): A {
  val functions = generateCFunction(serializer.descriptor)
  return prompt(
    prompt,
    functions,
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
}

@OptIn(ExperimentalSerializationApi::class)
private fun generateCFunction(descriptor: SerialDescriptor): List<CFunction> {
  val fnName = descriptor.serialName.substringAfterLast(".")
  return listOf(CFunction(fnName, "Generated function for $fnName", encodeJsonSchema(descriptor)))
}
