package com.xebia.functional.xef.llm

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import arrow.fx.coroutines.parMapNotNull
import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.*
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.AIEvent
import com.xebia.functional.xef.Config
import com.xebia.functional.xef.Tool
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.conversation.Description
import com.xebia.functional.xef.conversation.Schema
import com.xebia.functional.xef.llm.models.functions.buildJsonSchema
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder.Companion.tool
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.*

fun chatFunction(descriptor: SerialDescriptor): FunctionObject {
  val functionName = functionName(descriptor)
  return FunctionObject(
    name = functionName,
    description = functionDescription(descriptor, functionName),
    parameters = functionSchema(descriptor)
  )
}

@OptIn(ExperimentalSerializationApi::class)
fun functionSchema(descriptor: SerialDescriptor): JsonObject =
  descriptor.annotations.filterIsInstance<Schema>().firstOrNull()?.value?.let {
    Config.DEFAULT.json.decodeFromString(JsonObject.serializer(), it)
  } ?: buildJsonSchema(descriptor)

@OptIn(ExperimentalSerializationApi::class)
fun functionDescription(descriptor: SerialDescriptor, fnName: String): String =
  (descriptor.annotations.filterIsInstance<Description>().firstOrNull()?.value
    ?: defaultFunctionDescription(fnName))

fun defaultFunctionDescription(fnName: String): String = "Generated function for $fnName"

@OptIn(ExperimentalSerializationApi::class)
fun functionName(descriptor: SerialDescriptor): String =
  descriptor.serialName.substringAfterLast(".")

data class UsageTracker(
  var llmCalls: Int = 0,
  var toolInvocations: Int = 0,
  var totalErrors: Int = 0,
  var inputTokens: Int = 0,
  var outputTokens: Int = 0,
  var totalTokens: Int = 0,
)

@AiDsl
suspend fun <A> Chat.prompt(
  prompt: Prompt,
  scope: Conversation,
  serializer: Tool<A>,
  tools: List<Tool<*>>,
  collector: ProducerScope<AIEvent<*>>? = null,
  usageTracker: UsageTracker = UsageTracker()
): A =
  when (serializer) {
    is Tool.Sealed -> promptSealed(prompt, scope, serializer, tools, collector, usageTracker)
    is Tool.FlowOfAIEventsSealed -> {
      promptSealed(prompt, scope, serializer.sealedSerializer, tools, collector, usageTracker)
    }
    else -> {
      promptWithFunctions(
        prompt = prompt,
        scope = scope,
        serializer = serializer,
        tools = listOf(serializer) + tools,
        collector = collector,
        usageTracker = usageTracker,
        acceptedSerializerNames = listOf(serializer.function.name)
      )
    }
  }.also {
    collector?.send(
      AIEvent.Stop(
        AIEvent.Stop.Usage(
          llmCalls = usageTracker.llmCalls,
          toolCalls = usageTracker.toolInvocations,
          inputTokens = usageTracker.inputTokens,
          outputTokens = usageTracker.outputTokens,
          totalTokens = usageTracker.totalTokens
        )
      )
    )
  }

private suspend fun <A> Chat.promptWithFunctions(
  prompt: Prompt,
  scope: Conversation,
  serializer: Tool<A>,
  tools: List<Tool<*>>,
  collector: ProducerScope<AIEvent<*>>?,
  usageTracker: UsageTracker,
  acceptedSerializerNames: List<String>,
  invokeSerializer: suspend (FunctionCall) -> A = { serializer.invoke(it) }
): A {
  usageTracker.llmCalls++
  validateMaxToolCallsPerRound(prompt, usageTracker)
  val result =
    promptWithResponse(prompt, scope, tools.map { it.function }) { response ->
      val responseUsage = response.usage
      usageTracker.inputTokens += responseUsage?.promptTokens ?: 0
      usageTracker.outputTokens += responseUsage?.completionTokens ?: 0
      usageTracker.totalTokens += responseUsage?.totalTokens ?: 0
      val calls = functionCalls(response)
      val serializerCall = calls.firstOrNull { it.functionName in acceptedSerializerNames }
      if (serializerCall != null) {
        usageTracker.toolInvocations++
        val result = invokeSerializer(serializerCall)
        collector?.send(AIEvent.Result(result))
        result
      } else {
        val callRequestedMessages = listOf(assistantRequestedCallMessage(calls))
        val resultMessages = callResultMessages(prompt, calls, tools, collector)
        repeat(resultMessages.size) { usageTracker.toolInvocations++ }
        val promptWithToolOutputs =
          prompt.copy(messages = prompt.messages + callRequestedMessages + resultMessages)
        // recurse until the assistant decides to call the serializer
        promptWithFunctions(
          promptWithToolOutputs,
          scope,
          serializer,
          tools,
          collector,
          usageTracker,
          acceptedSerializerNames,
        )
      }
    }
  return result
}

private fun validateMaxToolCallsPerRound(prompt: Prompt, usageTracker: UsageTracker) {
  if (usageTracker.toolInvocations >= prompt.configuration.maxToolCallsPerRound) {
    error(
      "Too many tool calls in this round: ${usageTracker.toolInvocations}, max allowed: ${prompt.configuration.maxToolCallsPerRound}"
    )
  }
}

private suspend fun callResultMessages(
  prompt: Prompt,
  calls: List<FunctionCall>,
  functions: List<Tool<*>>,
  collector: ProducerScope<AIEvent<*>>?
): List<ChatCompletionRequestMessage> =
  calls.parMapNotNull(concurrency = prompt.configuration.concurrentToolCallsPerRound) { call ->
    val tool = functions.firstOrNull { it.function.name == call.functionName }
    tool?.let { collector?.send(AIEvent.ToolExecutionRequest(it, call.arguments)) }
    val invokeTool = tool?.invoke
    val result = invokeTool?.invoke(call).toString()
    tool?.let { collector?.send(AIEvent.ToolExecutionResponse(it, result)) }
    tool(call.callId, result)
  }

private fun assistantRequestedCallMessage(
  calls: List<FunctionCall>
): ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage =
  ChatCompletionRequestMessage.CaseChatCompletionRequestAssistantMessage(
    ChatCompletionRequestAssistantMessage(
      role = ChatCompletionRequestAssistantMessage.Role.assistant,
      toolCalls =
        calls.map {
          ChatCompletionMessageToolCall(
            id = it.callId,
            function =
              ChatCompletionMessageToolCallFunction(
                name = it.functionName,
                arguments = it.arguments
              ),
            type = ChatCompletionMessageToolCall.Type.function
          )
        }
    )
  )

@AiDsl
private suspend fun <A> Chat.promptSealed(
  prompt: Prompt,
  scope: Conversation,
  serializer: Tool.Sealed<A>,
  tools: List<Tool<*>>,
  collector: ProducerScope<AIEvent<*>>? = null,
  usageTracker: UsageTracker = UsageTracker()
): A {
  val allTools = serializer.cases.map { it.tool } + tools
  val acceptedSerializerNames = serializer.cases.map { it.tool.function.name }
  return promptWithFunctions(
    prompt,
    scope,
    serializer,
    allTools,
    collector,
    usageTracker,
    acceptedSerializerNames
  ) { call ->
    val case =
      serializer.cases.firstOrNull { it.tool.function.name == call.functionName }
        ?: error("No case found for call: $call")
    case.tool.invoke(call) as A
  }
}

private fun functionCalls(response: CreateChatCompletionResponse): List<FunctionCall> =
  response.choices
    .flatMap { it.message.toolCalls.orEmpty() }
    .map { FunctionCall(it.id, it.function.name, it.function.arguments) }

@AiDsl
fun <A> Chat.promptStreaming(
  prompt: Prompt,
  scope: Conversation,
  serializer: Tool<A>,
  tools: List<Tool<*>>
): Flow<StreamedFunction<A>> =
  promptStreaming(prompt, scope, serializer.function) { json ->
    serializer.invoke(FunctionCall("", "", json))
  }

@AiDsl
private suspend fun <A> Chat.promptWithResponse(
  prompt: Prompt,
  scope: Conversation,
  functions: List<FunctionObject>,
  serializer: suspend (response: CreateChatCompletionResponse) -> A,
): A =
  scope.metric.promptSpan(prompt) {
    val promptWithFunctions = prompt.copy(functions = functions)
    val adaptedPrompt =
      PromptCalculator.adaptPromptToConversationAndModel(promptWithFunctions, scope)
    adaptedPrompt.addMetrics(scope)
    val request = createChatCompletionRequest(adaptedPrompt)
    tryDeserialize(serializer, promptWithFunctions.configuration.maxDeserializationAttempts) {
      val requestedMemories = prompt.messages.toMemory(scope)
      val response = createChatCompletion(request).addMetrics(scope)
      response.choices.addChoiceWithFunctionsToMemory(
        scope,
        requestedMemories,
        prompt.configuration.messagePolicy.addMessagesToConversation
      )
      response
    }
  }

private fun createChatCompletionRequest(adaptedPrompt: Prompt): CreateChatCompletionRequest =
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

private fun chatCompletionToolChoiceOption(adaptedPrompt: Prompt): ChatCompletionToolChoiceOption =
  if (adaptedPrompt.functions.size == 1)
    ChatCompletionToolChoiceOption.CaseChatCompletionNamedToolChoice(
      ChatCompletionNamedToolChoice(
        type = ChatCompletionNamedToolChoice.Type.function,
        // TODO review access to first
        function = ChatCompletionNamedToolChoiceFunction(adaptedPrompt.functions.first().name)
      )
    )
  else {
    if (adaptedPrompt.model is CreateChatCompletionRequestModel.Custom)
      ChatCompletionToolChoiceOption.CaseString("auto")
    else ChatCompletionToolChoiceOption.CaseString("required")
  }

private fun chatCompletionTools(adaptedPrompt: Prompt): List<ChatCompletionTool> =
  adaptedPrompt.functions.map {
    ChatCompletionTool(type = ChatCompletionTool.Type.function, function = it)
  }

@AiDsl
fun <A> Chat.promptStreaming(
  prompt: Prompt,
  scope: Conversation,
  function: FunctionObject,
  serializer: suspend (json: String) -> A,
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
  serializer: suspend (response: CreateChatCompletionResponse) -> A,
  maxDeserializationAttempts: Int,
  agent: suspend () -> CreateChatCompletionResponse
): A {
  val logger = KotlinLogging.logger {}
  for (currentAttempts in 1..maxDeserializationAttempts) {
    val result = agent()
    catch({
      return@tryDeserialize serializer(result)
    }) { e: Throwable ->
      val message =
        "Failed to deserialize result after $maxDeserializationAttempts attempts: ${e.message}, calls: $result"
      logger.warn { message }
      if (currentAttempts == maxDeserializationAttempts)
        throw AIError.JsonParsing(message, maxDeserializationAttempts, e.nonFatalOrThrow())
      // TODO else log attempt ?
    }
  }
  throw AIError.NoResponse()
}
