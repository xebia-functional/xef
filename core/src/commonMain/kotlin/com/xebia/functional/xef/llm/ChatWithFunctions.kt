package com.xebia.functional.xef.llm

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequestWithFunctions
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponseWithFunctions
import com.xebia.functional.xef.llm.models.chat.Message
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.functions.encodeJsonSchema
import com.xebia.functional.xef.prompt.Prompt
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json

interface ChatWithFunctions : Chat {

  suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequestWithFunctions
  ): ChatCompletionResponseWithFunctions

  @OptIn(ExperimentalSerializationApi::class)
  fun generateCFunction(descriptor: SerialDescriptor): List<CFunction> {
    val fnName = descriptor.serialName.substringAfterLast(".")
    return generateCFunction(fnName, encodeJsonSchema(descriptor))
  }

  fun generateCFunction(fnName: String, schema: String): List<CFunction> =
    listOf(CFunction(fnName, "Generated function for $fnName", schema))

  @AiDsl
  suspend fun <A> prompt(
    prompt: String,
    scope: Conversation,
    functions: List<CFunction> = emptyList(),
    serializer: (json: String) -> A,
    promptConfiguration: PromptConfiguration,
  ): A = prompt(prompt.toMessages(), scope, functions, serializer, promptConfiguration)

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    scope: Conversation,
    serializerName: String,
    jsonSchema: String,
    serializer: (json: String) -> A,
    functions: List<CFunction> = generateCFunction(serializerName, jsonSchema),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
  ): A = prompt(prompt.toMessages(), scope, functions, serializer, promptConfiguration)

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    scope: Conversation,
    serializer: (json: String) -> A,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
  ): A = prompt(prompt.toMessages(), scope, functions, serializer, promptConfiguration)

  @OptIn(ExperimentalSerializationApi::class)
  @AiDsl
  suspend fun <A, B> prompt(
    input: A,
    scope: Conversation,
    inputSerializer: KSerializer<A>,
    outputSerializer: KSerializer<B>,
    functions: List<CFunction> = generateCFunction(outputSerializer.descriptor),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
  ): B =
    prompt(
      listOf(
        Message.userMessage {
          "${inputSerializer.descriptor.serialName}(${Json.encodeToString(inputSerializer, input)})"
        },
      ),
      scope,
      functions,
      { json -> Json.decodeFromString(outputSerializer, json) },
      promptConfiguration
    )

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    scope: Conversation,
    serializer: KSerializer<A>,
    functions: List<CFunction> = generateCFunction(serializer.descriptor),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
  ): A =
    prompt(
      prompt.toMessages(),
      scope,
      functions,
      { json -> Json.decodeFromString(serializer, json) },
      promptConfiguration
    )

  @AiDsl
  suspend fun <A> prompt(
    messages: List<Message>,
    scope: Conversation,
    serializer: KSerializer<A>,
    functions: List<CFunction> = generateCFunction(serializer.descriptor),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
  ): A =
    prompt(
      messages,
      scope,
      functions,
      { json -> Json.decodeFromString(serializer, json) },
      promptConfiguration
    )

  @AiDsl
  suspend fun <A> prompt(
    messages: List<Message>,
    scope: Conversation,
    functions: List<CFunction> = emptyList(),
    serializer: (json: String) -> A,
    promptConfiguration: PromptConfiguration,
  ): A =
    tryDeserialize(serializer, promptConfiguration.maxDeserializationAttempts) {
      promptMessages(messages = messages, scope = scope, functions = functions, promptConfiguration)
    }

  private suspend fun <A> tryDeserialize(
    serializer: (json: String) -> A,
    maxDeserializationAttempts: Int,
    agent: suspend () -> List<String>
  ): A {
    val logger = KotlinLogging.logger {}
    (0 until maxDeserializationAttempts).forEach { currentAttempts ->
      val result = agent().firstOrNull() ?: throw AIError.NoResponse()
      catch({
        return@tryDeserialize serializer(result)
      }) { e: Throwable ->
        logger.warn { "Failed to deserialize result: $result with exception ${e.message}" }
        if (currentAttempts == maxDeserializationAttempts)
          throw AIError.JsonParsing(result, maxDeserializationAttempts, e.nonFatalOrThrow())
        // TODO else log attempt ?
      }
    }
    throw AIError.NoResponse()
  }
}
