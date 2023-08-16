package com.xebia.functional.xef.auto.llm.openai

import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.ChatWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
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
suspend inline fun <reified A> Conversation.prompt(
  model: ChatWithFunctions,
  question: String,
  json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A = prompt(model, Prompt(question), json, promptConfiguration)

/**
 * Run a [prompt] describes the task you want to solve within the context of [AIScope]. Returns a
 * value of [A] where [A] **has to be** annotated with [kotlinx.serialization.Serializable].
 *
 * @throws SerializationException if serializer cannot be created (provided [A] or its type argument
 *   is not serializable).
 * @throws IllegalArgumentException if any of [A]'s type arguments contains star projection.
 */
@AiDsl
suspend inline fun <reified A> Conversation.prompt(
  model: ChatWithFunctions,
  prompt: Prompt,
  json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A = prompt(model, prompt, serializer(), json, promptConfiguration)

@AiDsl
suspend fun <A> Conversation.prompt(
  model: ChatWithFunctions,
  prompt: Prompt,
  serializer: KSerializer<A>,
  json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A {
  val functions = model.generateCFunction(serializer.descriptor)
  return model.prompt(
    prompt,
    this,
    { json.decodeFromString(serializer, it) },
    functions,
    promptConfiguration
  )
}

/**
 * Run a [List<Message>] describes the task you want to solve within the context of [AIScope].
 * Returns a value of [A] where [A] **has to be** annotated with
 * [kotlinx.serialization.Serializable].
 *
 * @throws SerializationException if serializer cannot be created (provided [A] or its type argument
 *   is not serializable).
 * @throws IllegalArgumentException if any of [A]'s type arguments contains star projection.
 */
@AiDsl
suspend inline fun <reified A> Conversation.prompt(
  model: ChatWithFunctions,
  messages: List<Message>,
  json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A = prompt(model, messages, serializer(), json, promptConfiguration)

@AiDsl
suspend fun <A> Conversation.prompt(
  model: ChatWithFunctions,
  messages: List<Message>,
  serializer: KSerializer<A>,
  json: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  },
  promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
): A {
  val functions = model.generateCFunction(serializer.descriptor)
  return model.prompt(
    messages = messages,
    scope = this,
    functions = functions,
    serializer = { json.decodeFromString(serializer, it) },
    promptConfiguration = promptConfiguration
  )
}
