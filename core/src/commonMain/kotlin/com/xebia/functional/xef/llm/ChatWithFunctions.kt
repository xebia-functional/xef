package com.xebia.functional.xef.llm

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.Conversation
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequestWithFunctions
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponseWithFunctions
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.functions.encodeJsonSchema
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.user
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.Json

interface ChatWithFunctions : Chat {

  suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequestWithFunctions
  ): ChatCompletionResponseWithFunctions

  @OptIn(ExperimentalSerializationApi::class)
  fun chatFunctions(descriptor: SerialDescriptor): List<CFunction> {
    val fnName = descriptor.serialName.substringAfterLast(".")
    return chatFunctions(fnName, encodeJsonSchema(descriptor))
  }

  fun chatFunctions(fnName: String, schema: String): List<CFunction> =
    listOf(CFunction(fnName, "Generated function for $fnName", schema))

  @OptIn(ExperimentalSerializationApi::class)
  @AiDsl
  suspend fun <A, B> prompt(
    input: A,
    scope: Conversation,
    inputSerializer: KSerializer<A>,
    outputSerializer: KSerializer<B>
  ): B =
    when (outputSerializer.descriptor.kind) {
      PrimitiveKind.STRING ->
        promptMessage(prompt = Prompt { +user(encodeInput(inputSerializer, input)) }, scope = scope)
          as B
      else -> prompt(Prompt { +user(encodeInput(inputSerializer, input)) }, scope, outputSerializer)
    }

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    scope: Conversation,
    serializer: KSerializer<A>,
  ): A =
    prompt(prompt, scope, chatFunctions(serializer.descriptor)) { json ->
      Json.decodeFromString(serializer, json)
    }

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    scope: Conversation,
    functions: List<CFunction>,
    serializer: (json: String) -> A,
  ): A {
    val promptWithFunctions = prompt.copy(functions = functions)
    return tryDeserialize(
      serializer,
      promptWithFunctions.configuration.maxDeserializationAttempts
    ) {
      promptMessages(prompt = promptWithFunctions, scope = scope)
    }
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

  @OptIn(ExperimentalSerializationApi::class)
  private fun <A> encodeInput(inputSerializer: KSerializer<A>, input: A): String =
    when (inputSerializer.descriptor.kind) {
      PrimitiveKind.STRING -> input.toString()
      else -> Json.encodeToString(inputSerializer, input)
    }
}
