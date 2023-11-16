package com.xebia.functional.xef.llm

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.*
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.functions.buildJsonSchema
import com.xebia.functional.xef.prompt.Prompt
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
fun chatFunction(descriptor: SerialDescriptor): FunctionObject {
  val fnName = descriptor.serialName.substringAfterLast(".")
  return chatFunction(fnName, buildJsonSchema(descriptor))
}

fun chatFunction(fnName: String, schema: JsonObject): FunctionObject =
  FunctionObject(fnName, schema, "Generated function for $fnName")

@AiDsl
suspend fun <A> ChatApi.prompt(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  serializer: KSerializer<A>,
): A =
  prompt(prompt, scope, chatFunction(serializer.descriptor)) { json ->
    Json.decodeFromString(serializer, json)
  }

@AiDsl
fun <A> ChatApi.promptStreaming(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  serializer: KSerializer<A>,
): Flow<StreamedFunction<A>> =
  promptStreaming(prompt, scope, chatFunction(serializer.descriptor)) { json ->
    Json.decodeFromString(serializer, json)
  }

@AiDsl
suspend fun <A> ChatApi.prompt(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  function: FunctionObject,
  serializer: (json: String) -> A,
): A =
  scope.metric.promptSpan(prompt) {
    val promptWithFunctions = prompt.copy(function = function)
    val adaptedPrompt =
      PromptCalculator.adaptPromptToConversationAndModel(promptWithFunctions, scope)

    adaptedPrompt.addMetrics(scope)

    val request =
      CreateChatCompletionRequest(
        user = adaptedPrompt.configuration.user,
        messages = adaptedPrompt.messages,
        n = adaptedPrompt.configuration.numberOfPredictions,
        temperature = adaptedPrompt.configuration.temperature,
        maxTokens = adaptedPrompt.configuration.minResponseTokens,
        tools =
          listOf(
            ChatCompletionTool(
              type = ChatCompletionTool.Type.function,
              function = adaptedPrompt.function!!
            )
          ),
        toolChoice =
          ChatCompletionToolChoiceOption(
            type = ChatCompletionToolChoiceOption.Type.function,
            function = ChatCompletionNamedToolChoiceFunction(adaptedPrompt.function.name)
          ),
        model = prompt.model,
      )

    tryDeserialize(serializer, promptWithFunctions.configuration.maxDeserializationAttempts) {
      val requestedMemories = prompt.messages.toMemory(scope)
      createChatCompletion(request)
        .body()
        .addMetrics(scope)
        .choices
        .addChoiceWithFunctionsToMemory(
          scope,
          requestedMemories,
          prompt.configuration.messagePolicy.addMessagesToConversation
        )
        .mapNotNull { it.message.toolCalls?.firstOrNull()?.function?.arguments }
    }
  }

@AiDsl
fun <A> ChatApi.promptStreaming(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  function: FunctionObject,
  serializer: (json: String) -> A,
): Flow<StreamedFunction<A>> = flow {
  val promptWithFunctions = prompt.copy(function = function)
  val messagesForRequestPrompt =
    PromptCalculator.adaptPromptToConversationAndModel(promptWithFunctions, scope)

  val request =
    CreateChatCompletionRequest(
      stream = true,
      user = promptWithFunctions.configuration.user,
      messages = messagesForRequestPrompt.messages,
      n = promptWithFunctions.configuration.numberOfPredictions,
      temperature = promptWithFunctions.configuration.temperature,
      maxTokens = promptWithFunctions.configuration.minResponseTokens,
      tools =
        listOf(
          ChatCompletionTool(
            type = ChatCompletionTool.Type.function,
            function = promptWithFunctions.function!!
          )
        ),
      toolChoice =
        ChatCompletionToolChoiceOption(
          type = ChatCompletionToolChoiceOption.Type.function,
          function = ChatCompletionNamedToolChoiceFunction(promptWithFunctions.function.name)
        ),
      model = prompt.model,
    )

  StreamedFunction.run {
    retryUntilMaxDeserializationAttempts(
      promptWithFunctions.configuration.maxDeserializationAttempts
    ) {
      streamFunctionCall(
        chat = this@promptStreaming,
        prompt = prompt,
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
