package com.xebia.functional.xef.llm

import ai.xef.Chat
import ai.xef.Model
import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.functions.buildJsonSchema
import com.xebia.functional.xef.prompt.Prompt
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.*

data class FunctionObject(val name: String, val description: String, val schema: JsonObject)

data class CreateChatCompletionRequest(
  val user: String,
  val messages: List<ChatCompletionRequestMessage>,
  val n: Int,
  val temperature: Double,
  val maxTokens: Int,
  val tools: List<ChatCompletionTool>,
  val toolChoice: ToolChoiceOption?,
  val logitBias: Map<String, Int>? = null,
  val model: Model,
  val seed: Int?,
  val stream: Boolean,
)

data class ToolCallResults(
  val toolCallId: String,
  val toolCallName: String,
  val result: String,
)


data class ChatCompletionRequestMessage(
  val role: Role,
  val content: String,
  val toolCallResults: ToolCallResults?
)

data class ChatCompletionTool(
  val type: Type,
  val function: FunctionObject,
) {
  enum class Type {
    function
  }
}

sealed class ToolChoiceOption {
  data class ChatCompletionNamedToolChoiceFunction(val name: String) : ToolChoiceOption()
  object Auto : ToolChoiceOption()
}

data class CreateChatCompletionResponse (
  /* A unique identifier for the chat completion. */
  @SerialName(value = "id") val id: kotlin.String,
  /* A list of chat completion choices. Can be more than one if `n` is greater than 1. */
  @SerialName(value = "choices") val choices: kotlin.collections.List<CreateChatCompletionResponseChoicesInner>,
  /* The Unix timestamp (in seconds) of when the chat completion was created. */
  @SerialName(value = "created") val created: kotlin.Int,
  /* The model used for the chat completion. */
  @SerialName(value = "model") val model: kotlin.String,
  /* The object type, which is always `chat.completion`. */
  /* This fingerprint represents the backend configuration that the model runs with.  Can be used in conjunction with the `seed` request parameter to understand when backend changes have been made that might impact determinism.  */
  @SerialName(value = "system_fingerprint") val systemFingerprint: kotlin.String? = null,
  @SerialName(value = "usage") val usage: Usage? = null
)

data class Usage(
  val inputTokenCount: Int,
  val outputTokenCount: Int,
  val totalTokenCount: Int
)

data class ChatCompletionResponseChoiceMessageToolCall(
  val function: ChatCompletionResponseChoiceMessageToolCallFunction,
)

data class ChatCompletionResponseChoiceMessageToolCallFunction(
  val name: String,
  val arguments: String,
)


@OptIn(ExperimentalSerializationApi::class)
fun chatFunction(descriptor: SerialDescriptor): FunctionObject {
  val fnName = descriptor.serialName.substringAfterLast(".")
  return chatFunction(fnName, buildJsonSchema(descriptor))
}

fun chatFunctions(descriptors: List<SerialDescriptor>): List<FunctionObject> =
  descriptors.map(::chatFunction)

fun chatFunction(fnName: String, schema: JsonObject): FunctionObject =
  FunctionObject(fnName, "Generated function for $fnName", schema)


internal suspend inline fun <reified A> Chat.prompt(
  prompt: Prompt,
  scope: Conversation = Conversation()
): A =
  prompt(prompt, scope, serializer())


internal suspend fun <A> Chat.prompt(
  prompt: Prompt,
  scope: Conversation,
  serializer: KSerializer<A>,
): A =
  prompt(prompt, scope, chatFunctions(listOf(serializer.descriptor))) { call ->
    Json.decodeFromString(serializer, call.arguments)
  }

@OptIn(ExperimentalSerializationApi::class)

internal suspend fun <A> Chat.prompt(
  prompt: Prompt,
  scope: Conversation,
  serializer: KSerializer<A>,
  descriptors: List<SerialDescriptor>,
): A =
  prompt(prompt, scope, chatFunctions(descriptors)) { call ->
    // adds a `type` field with the call.functionName serial name equivalent to the call arguments
    val jsonWithDiscriminator = Json.decodeFromString(JsonElement.serializer(), call.arguments)
    val descriptor =
      descriptors.firstOrNull { it.serialName.endsWith(call.functionName) }
        ?: error("No descriptor found for ${call.functionName}")
    val newJson =
      JsonObject(
        jsonWithDiscriminator.jsonObject + ("type" to JsonPrimitive(descriptor.serialName))
      )
    Json.decodeFromString(serializer, Json.encodeToString(newJson))
  }


internal fun <A> Chat.promptStreaming(
  prompt: Prompt,
  scope: Conversation,
  serializer: KSerializer<A>,
): Flow<StreamedFunction<A>> =
  promptStreaming(prompt, scope, chatFunction(serializer.descriptor)) { json ->
    Json.decodeFromString(serializer, json)
  }


internal suspend fun <A> Chat.prompt(
  prompt: Prompt,
  scope: Conversation,
  functions: List<FunctionObject>,
  serializer: (call: ToolCall) -> A,
): A =
  scope.metric.promptSpan(prompt) {
    val promptWithFunctions = prompt.copy(functions = functions)
    val adaptedPrompt =
      PromptCalculator.adaptPromptToConversationAndModel(this@prompt, promptWithFunctions, scope)
    adaptedPrompt.addMetrics(scope)
    val request = createChatCompletionRequest(adaptedPrompt)
    tryDeserialize(serializer, promptWithFunctions.configuration.maxDeserializationAttempts) {
      val requestedMemories = prompt.messages.toMemory(scope)
      createChatCompletion(request)
        .addMetrics(scope)
        .choices
        .addChoiceWithFunctionsToMemory(
          scope,
          requestedMemories,
          prompt.configuration.messagePolicy.addMessagesToConversation
        )
        .mapNotNull {
          val functionName = it.message.toolCalls?.firstOrNull()?.function?.functionName
          val arguments = it.message.toolCalls?.firstOrNull()?.function?.arguments
          if (functionName != null && arguments != null) {
            ToolCall(functionName, arguments)
          } else null
        }
    }
  }

private fun Chat.createChatCompletionRequest(adaptedPrompt: Prompt): CreateChatCompletionRequest =
  CreateChatCompletionRequest(
    user = adaptedPrompt.configuration.user,
    messages = adaptedPrompt.messages,
    n = adaptedPrompt.configuration.numberOfPredictions,
    temperature = adaptedPrompt.configuration.temperature,
    maxTokens = adaptedPrompt.configuration.maxTokens,
    tools = chatCompletionTools(adaptedPrompt),
    toolChoice = chatCompletionToolChoiceOption(adaptedPrompt),
    model = this,
    seed = adaptedPrompt.configuration.seed,
    stream = false
  )

private fun chatCompletionToolChoiceOption(adaptedPrompt: Prompt): ToolChoiceOption =
  if (adaptedPrompt.functions.size == 1)
    ToolChoiceOption.ChatCompletionNamedToolChoiceFunction(
      adaptedPrompt.functions.first().name
    )
  else ToolChoiceOption.Auto

private fun chatCompletionTools(adaptedPrompt: Prompt): List<ChatCompletionTool> =
  adaptedPrompt.functions.map {
    ChatCompletionTool(type = ChatCompletionTool.Type.function, function = it)
  }

internal fun <A> Chat.promptStreaming(
  prompt: Prompt,
  scope: Conversation,
  function: FunctionObject,
  serializer: (json: String) -> A,
): Flow<StreamedFunction<A>> = flow {
  val promptWithFunctions = prompt.copy(functions = listOf(function))
  val adaptedPrompt = PromptCalculator.adaptPromptToConversationAndModel(this@promptStreaming, promptWithFunctions, scope)

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
  serializer: (call: ToolCall) -> A,
  maxDeserializationAttempts: Int,
  agent: suspend () -> List<ToolCall>
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
