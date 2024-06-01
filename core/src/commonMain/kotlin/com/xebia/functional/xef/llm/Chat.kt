package com.xebia.functional.xef.llm

import ai.xef.Chat
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.MessageWithUsage
import com.xebia.functional.xef.llm.models.MessagesUsage
import com.xebia.functional.xef.llm.models.MessagesWithUsage
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import com.xebia.functional.xef.store.Memory
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsName

data class CreateChatCompletionStreamResponse (
  /* A unique identifier for the chat completion. Each chunk has the same ID. */
  @SerialName(value = "id") val id: kotlin.String,
  /* A list of chat completion choices. Can contain more than one elements if `n` is greater than 1. Can also be empty for the last chunk if you set `stream_options: {\"include_usage\": true}`.  */
  @SerialName(value = "choices") val choices: kotlin.collections.List<CreateChatCompletionStreamResponseChoicesInner>,
  /* The Unix timestamp (in seconds) of when the chat completion was created. Each chunk has the same timestamp. */
  @SerialName(value = "created") val created: kotlin.Int,
  /* The model to generate the completion. */
  @SerialName(value = "model") val model: kotlin.String,
  /* The object type, which is always `chat.completion.chunk`. */
  /* This fingerprint represents the backend configuration that the model runs with. Can be used in conjunction with the `seed` request parameter to understand when backend changes have been made that might impact determinism.  */
  @SerialName(value = "system_fingerprint") val systemFingerprint: kotlin.String? = null,
  @SerialName(value = "usage") val usage: CreateChatCompletionStreamResponseUsage? = null
)

data class CreateChatCompletionStreamResponseUsage (
  /* Number of tokens in the generated completion. */
  @SerialName(value = "completion_tokens") val completionTokens: kotlin.Int,
  /* Number of tokens in the prompt. */
  @SerialName(value = "prompt_tokens") val promptTokens: kotlin.Int,
  /* Total number of tokens used in the request (prompt + completion). */
  @SerialName(value = "total_tokens") val totalTokens: kotlin.Int
)

data class ChatCompletionMessageToolCallChunk (
  /* The type of the tool. Currently, only `function` is supported. */
  @SerialName(value = "function") val function: ToolCall? = null
)

data class ChatCompletionStreamResponseDelta (
  /* The contents of the chunk message. */
  @SerialName(value = "content") val content: kotlin.String? = null,
  @Deprecated(message = "This property is deprecated.")
  @SerialName(value = "function_call") val toolCall: ToolCall? = null,
  @SerialName(value = "tool_calls") val toolCalls: kotlin.collections.List<ChatCompletionMessageToolCallChunk>? = null,
  /* The role of the author of this message. */
  @SerialName(value = "role") val role: Role? = null
)

enum class Role {
  @SerialName(value = "system") system,
  @SerialName(value = "user") user,
  @SerialName(value = "assistant") assistant,
  @SerialName(value = "tool") tool

}




/**
 * The reason the model stopped generating tokens. This will be `stop` if the model hit a natural stop point or a provided stop sequence, `length` if the maximum number of tokens specified in the request was reached, `content_filter` if content was omitted due to a flag from our content filters, `tool_calls` if the model called a tool, or `function_call` (deprecated) if the model called a function.
 *
 * Values: stop,length,tool_calls,content_filter,function_call
 */
@Serializable
enum class FinishReason(val value: kotlin.String) {
  @SerialName(value = "stop") stop("stop"),
  @SerialName(value = "length") @JsName("length_type") length("length"),
  @SerialName(value = "tool_calls") tool_calls("tool_calls"),
  @SerialName(value = "content_filter") content_filter("content_filter"),
  @SerialName(value = "function_call") function_call("function_call"),
  @SerialName(value = "other") other("other");
}

data class CreateChatCompletionStreamResponseChoicesInner (
  @SerialName(value = "delta") val delta: ChatCompletionStreamResponseDelta,
  /* The reason the model stopped generating tokens. This will be `stop` if the model hit a natural stop point or a provided stop sequence, `length` if the maximum number of tokens specified in the request was reached, `content_filter` if content was omitted due to a flag from our content filters, `tool_calls` if the model called a tool, or `function_call` (deprecated) if the model called a function.  */
  @SerialName(value = "finish_reason") val finishReason: FinishReason?,
)

data class CreateChatCompletionResponseChoicesInner (
  /* The reason the model stopped generating tokens. This will be `stop` if the model hit a natural stop point or a provided stop sequence, `length` if the maximum number of tokens specified in the request was reached, `content_filter` if content was omitted due to a flag from our content filters, `tool_calls` if the model called a tool, or `function_call` (deprecated) if the model called a function.  */
  @SerialName(value = "finish_reason") val finishReason: FinishReason,
  @SerialName(value = "message") val message: ChatCompletionResponseMessage
)

data class ChatCompletionMessageToolCall (
  /* The ID of the tool call. */
  @SerialName(value = "id") val id: kotlin.String,
  @SerialName(value = "function") val function: ToolCall
)

data class ChatCompletionResponseMessage (
  /* The contents of the message. */
  @SerialName(value = "content") val content: kotlin.String?,
  /* The role of the author of this message. */
  @SerialName(value = "role") val role: Role,
  /* The tool calls generated by the model, such as function calls. */
  @SerialName(value = "tool_calls") val toolCalls: kotlin.collections.List<ChatCompletionMessageToolCall>? = null,
)

@AiDsl
internal fun Chat.promptStreaming(prompt: Prompt, scope: Conversation = Conversation()): Flow<String> =
  flow {
    val messagesForRequestPrompt = PromptCalculator.adaptPromptToConversationAndModel(this@promptStreaming, prompt, scope)

    val request =
      CreateChatCompletionRequest(
        stream = true,
        user = prompt.configuration.user,
        messages = messagesForRequestPrompt.messages,
        n = prompt.configuration.numberOfPredictions,
        temperature = prompt.configuration.temperature,
        maxTokens = prompt.configuration.maxTokens,
        model = this@promptStreaming,
        seed = prompt.configuration.seed,
        tools = prompt.functions.map { ChatCompletionTool(type = ChatCompletionTool.Type.function, function = it) },
        toolChoice = if (prompt.functions.size == 1) {
          ToolChoiceOption.ChatCompletionNamedToolChoiceFunction(prompt.functions.first().name)
        } else {
          ToolChoiceOption.Auto
        }
      )

    val buffer = StringBuilder()

    this@promptStreaming.createChatCompletionStream(request)
      .mapNotNull {
        val content = it.choices.firstOrNull()?.delta?.content
        if (content != null) {
          buffer.append(content)
        }
        content
      }
      .onEach { emit(it) }
      .onCompletion {
        val aiResponseMessage = PromptBuilder.assistant(buffer.toString())
        val newMessages = prompt.messages + listOf(aiResponseMessage)
        newMessages.addToMemory(scope, prompt.configuration.messagePolicy.addMessagesToConversation)
        buffer.clear()
      }
      .collect()
  }

@AiDsl
internal suspend fun Chat.promptMessage(prompt: Prompt, scope: Conversation = Conversation()): String =
  promptMessages(prompt, scope).firstOrNull() ?: throw AIError.NoResponse()

@AiDsl
internal suspend fun Chat.promptMessages(
  prompt: Prompt,
  scope: Conversation = Conversation()
): List<String> = promptResponse(prompt, scope) { it.message.content }.first

@AiDsl
internal suspend fun Chat.promptMessageAndUsage(
  prompt: Prompt,
  scope: Conversation = Conversation()
): MessageWithUsage {
  val response = promptMessagesAndUsage(prompt, scope)
  val message = response.messages.firstOrNull() ?: throw AIError.NoResponse()
  return MessageWithUsage(message, response.usage)
}

@AiDsl
internal suspend fun Chat.promptMessagesAndUsage(
  prompt: Prompt,
  scope: Conversation = Conversation()
): MessagesWithUsage {
  val response = promptResponse(prompt, scope) { it.message.content }
  return MessagesWithUsage(response.first, response.second.usage?.let { MessagesUsage(it) })
}

private suspend fun <T> Chat.promptResponse(
  prompt: Prompt,
  scope: Conversation = Conversation(),
  block: suspend Chat.(CreateChatCompletionResponseChoicesInner) -> T?
): Pair<List<T>, CreateChatCompletionResponse> =
  scope.metric.promptSpan(prompt) {
    val promptMemories: List<Memory> = prompt.messages.toMemory(scope)
    val adaptedPrompt = PromptCalculator.adaptPromptToConversationAndModel(this@promptResponse, prompt, scope)

    adaptedPrompt.addMetrics(scope)

    val request =
      CreateChatCompletionRequest(
        user = adaptedPrompt.configuration.user,
        messages = adaptedPrompt.messages,
        n = adaptedPrompt.configuration.numberOfPredictions,
        temperature = adaptedPrompt.configuration.temperature,
        maxTokens = adaptedPrompt.configuration.maxTokens,
        model = this@promptResponse,
        seed = adaptedPrompt.configuration.seed,
        tools = prompt.functions.map { ChatCompletionTool(type = ChatCompletionTool.Type.function, function = it) },
        toolChoice = if (prompt.functions.size == 1) {
          ToolChoiceOption.ChatCompletionNamedToolChoiceFunction(prompt.functions.first().name)
        } else {
          ToolChoiceOption.Auto
        },
        stream = false
      )

    val createResponse: CreateChatCompletionResponse = createChatCompletion(request)
    Pair(
      createResponse
        .addMetrics(scope)
        .choices
        .addChoiceToMemory(
          scope,
          promptMemories,
          prompt.configuration.messagePolicy.addMessagesToConversation
        )
        .mapNotNull { block(it) },
      createResponse
    )
  }
