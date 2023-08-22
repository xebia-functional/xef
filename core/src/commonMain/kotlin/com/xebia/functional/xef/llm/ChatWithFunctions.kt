package com.xebia.functional.xef.llm

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequest
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponseWithFunctions
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.llm.models.functions.encodeJsonSchema
import com.xebia.functional.xef.prompt.Prompt
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*

interface ChatWithFunctions : Chat {

  suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequest
  ): ChatCompletionResponseWithFunctions

  @OptIn(ExperimentalSerializationApi::class)
  fun chatFunction(descriptor: SerialDescriptor): CFunction {
    val fnName = descriptor.serialName.substringAfterLast(".")
    return chatFunction(fnName, encodeJsonSchema(descriptor))
  }

  fun chatFunction(fnName: String, schema: String): CFunction =
    CFunction(fnName, "Generated function for $fnName", schema)

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    scope: Conversation,
    serializer: KSerializer<A>,
  ): A =
    prompt(prompt, scope, chatFunction(serializer.descriptor)) { json ->
      Json.decodeFromString(serializer, json)
    }

  @AiDsl
  fun <A> promptStreaming(
    prompt: Prompt,
    scope: Conversation,
    serializer: KSerializer<A>,
  ): Flow<StreamedFunction<A>> =
    promptStreaming(prompt, scope, chatFunction(serializer.descriptor)) { json ->
      Json.decodeFromString(serializer, json)
    }

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    scope: Conversation,
    function: CFunction,
    serializer: (json: String) -> A,
  ): A {
    val promptWithFunctions = prompt.copy(function = function)
    return tryDeserialize(
      serializer,
      promptWithFunctions.configuration.maxDeserializationAttempts
    ) {
      promptMessages(prompt = promptWithFunctions, scope = scope)
    }
  }

  @AiDsl
  fun <A> promptStreaming(
    prompt: Prompt,
    scope: Conversation,
    function: CFunction,
    serializer: (json: String) -> A,
  ): Flow<StreamedFunction<A>> = flow {
    val promptWithFunctions = prompt.copy(function = function)
    val messagesForRequestPrompt =
      PromptCalculator.adaptPromptToConversationAndModel(
        promptWithFunctions,
        scope,
        this@ChatWithFunctions
      )

    val request =
      ChatCompletionRequest(
        model = name,
        stream = true,
        user = promptWithFunctions.configuration.user,
        messages = messagesForRequestPrompt.messages,
        functions = listOfNotNull(messagesForRequestPrompt.function),
        n = promptWithFunctions.configuration.numberOfPredictions,
        temperature = promptWithFunctions.configuration.temperature,
        maxTokens = promptWithFunctions.configuration.minResponseTokens,
        functionCall = mapOf("name" to (promptWithFunctions.function?.name ?: ""))
      )

    StreamedFunction.run {
      retryUntilMaxDeserializationAttempts(
        promptWithFunctions.configuration.maxDeserializationAttempts
      ) {
        streamFunctionCall(
          chat = this@ChatWithFunctions,
          request = request,
          scope = scope,
          serializer = serializer,
          function = function
        )
      }
    }
  }

  private suspend fun retryUntilMaxDeserializationAttempts(
    maxDeserializationAttempts: Int,
    block: suspend () -> Unit
  ): Unit {
    var success = false
    var attempts = 0
    while (!success) {
      try {
        block()
        success = true
      } catch (e: Throwable) {
        attempts++
        if (attempts == maxDeserializationAttempts) {
          throw e
        }
      }
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
}
