package com.xebia.functional.xef.llm

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.infrastructure.ApiClient
import com.xebia.functional.openai.models.*
import com.xebia.functional.openai.models.ext.chat.ChatCompletionToolChoiceOption
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
fun chatFunction(descriptor: SerialDescriptor): FunctionObject {
  val fnName = descriptor.serialName.substringAfterLast(".")
  return chatFunction(fnName, buildJsonSchema(descriptor))
}

fun chatFunctions(descriptors: List<SerialDescriptor>): List<FunctionObject> =
  descriptors.map(::chatFunction)

fun chatFunction(fnName: String, schema: JsonObject): FunctionObject =
  FunctionObject(fnName, "Generated function for $fnName", schema)

@AiDsl
suspend fun <A> ChatApi.prompt(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  serializer: KSerializer<A>,
): A =
  prompt(prompt, scope, chatFunctions(listOf(serializer.descriptor))) { call ->
    ApiClient.JSON_DEFAULT.decodeFromString(serializer, call.arguments)
  }

@OptIn(ExperimentalSerializationApi::class)
@AiDsl
suspend fun <A> ChatApi.prompt(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  serializer: KSerializer<A>,
  descriptors: List<SerialDescriptor>,
): A =
  prompt(prompt, scope, chatFunctions(descriptors)) { call ->
    // adds a `type` field with the call.functionName serial name equivalent to the call arguments
    val jsonWithDiscriminator =
      ApiClient.JSON_DEFAULT.decodeFromString(JsonElement.serializer(), call.arguments)
    val descriptor =
      descriptors.firstOrNull { it.serialName.endsWith(call.functionName) }
        ?: error("No descriptor found for ${call.functionName}")
    val newJson =
      JsonObject(
        jsonWithDiscriminator.jsonObject + ("type" to JsonPrimitive(descriptor.serialName))
      )
    ApiClient.JSON_DEFAULT.decodeFromString(
      serializer,
      ApiClient.JSON_DEFAULT.encodeToString(newJson)
    )
  }

@AiDsl
fun <A> ChatApi.promptStreaming(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  serializer: KSerializer<A>,
): Flow<StreamedFunction<A>> =
  promptStreaming(prompt, scope, chatFunction(serializer.descriptor)) { json ->
    ApiClient.JSON_DEFAULT.decodeFromString(serializer, json)
  }

@AiDsl
suspend fun <A> ChatApi.prompt(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  functions: List<FunctionObject>,
  serializer: (call: FunctionCall) -> A,
): A =
  scope.metric.promptSpan(prompt) {
    val promptWithFunctions = prompt.copy(functions = functions)
    val adaptedPrompt =
      PromptCalculator.adaptPromptToConversationAndModel(promptWithFunctions, scope)
    adaptedPrompt.addMetrics(scope)
    val request = createChatCompletionRequest(adaptedPrompt)
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
        .mapNotNull {
          val functionName = it.message.toolCalls?.firstOrNull()?.function?.name
          val arguments = it.message.toolCalls?.firstOrNull()?.function?.arguments
          if (functionName != null && arguments != null) {
            FunctionCall(functionName, arguments)
          } else null
        }
    }
  }

private fun createChatCompletionRequest(
  adaptedPrompt: Prompt<CreateChatCompletionRequestModel>
): CreateChatCompletionRequest =
  CreateChatCompletionRequest(
    user = adaptedPrompt.configuration.user,
    messages = adaptedPrompt.messages,
    n = adaptedPrompt.configuration.numberOfPredictions,
    temperature = adaptedPrompt.configuration.temperature,
    maxTokens = adaptedPrompt.configuration.maxTokens,
    tools = chatCompletionTools(adaptedPrompt),
    toolChoice = chatCompletionToolChoiceOption(adaptedPrompt),
    model = adaptedPrompt.model,
    seed = adaptedPrompt.configuration.seed,
  )

private fun chatCompletionToolChoiceOption(
  adaptedPrompt: Prompt<CreateChatCompletionRequestModel>
): ChatCompletionToolChoiceOption =
  if (adaptedPrompt.functions.size == 1)
    ChatCompletionToolChoiceOption.function(
      ChatCompletionNamedToolChoiceFunction(adaptedPrompt.functions.first().name)
    )
  else ChatCompletionToolChoiceOption.auto

private fun chatCompletionTools(
  adaptedPrompt: Prompt<CreateChatCompletionRequestModel>
): List<ChatCompletionTool> =
  adaptedPrompt.functions.map {
    ChatCompletionTool(type = ChatCompletionTool.Type.function, function = it)
  }

@AiDsl
fun <A> ChatApi.promptStreaming(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation,
  function: FunctionObject,
  serializer: (json: String) -> A,
): Flow<StreamedFunction<A>> = flow {
  val promptWithFunctions = prompt.copy(functions = listOf(function))
  val adaptedPrompt = PromptCalculator.adaptPromptToConversationAndModel(promptWithFunctions, scope)

  val request = createChatCompletionRequest(adaptedPrompt).copy(stream = true)

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
  serializer: (call: FunctionCall) -> A,
  maxDeserializationAttempts: Int,
  agent: suspend () -> List<FunctionCall>
): A {
  val logger = KotlinLogging.logger {}
  for (currentAttempts in 1..maxDeserializationAttempts) {
    val result = agent().firstOrNull() ?: throw AIError.NoResponse()
    catch({
      return@tryDeserialize serializer(result)
    }) { e: Throwable ->
      logger.warn { "Failed to deserialize result: $result with exception ${e.message}" }
      if (currentAttempts == maxDeserializationAttempts)
        throw AIError.JsonParsing(result.arguments, maxDeserializationAttempts, e.nonFatalOrThrow())
      // TODO else log attempt ?
    }
  }
  throw AIError.NoResponse()
}
