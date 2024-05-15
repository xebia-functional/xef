package com.xebia.functional.xef.llm

import com.xebia.functional.openai.generated.api.Chat
import com.xebia.functional.openai.generated.model.CreateChatCompletionRequest
import com.xebia.functional.openai.generated.model.CreateChatCompletionResponse
import com.xebia.functional.openai.generated.model.CreateChatCompletionResponseChoicesInner
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.PromptCalculator.adaptPromptToConversationAndModel
import com.xebia.functional.xef.llm.models.MessageWithUsage
import com.xebia.functional.xef.llm.models.MessagesUsage
import com.xebia.functional.xef.llm.models.MessagesWithUsage
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.PromptBuilder
import com.xebia.functional.xef.store.Memory
import kotlinx.coroutines.flow.*

@AiDsl
fun Chat.promptStreaming(prompt: Prompt, scope: Conversation = Conversation()): Flow<String> =
  flow {
    val messagesForRequestPrompt = prompt.adaptPromptToConversationAndModel(scope)

    val request =
      CreateChatCompletionRequest(
        stream = true,
        user = prompt.configuration.user,
        messages = messagesForRequestPrompt.messages,
        n = prompt.configuration.numberOfPredictions,
        temperature = prompt.configuration.temperature,
        maxTokens = prompt.configuration.maxTokens,
        model = prompt.model,
        seed = prompt.configuration.seed,
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
      .onCompletion {
        val aiResponseMessage = PromptBuilder.assistant(buffer.toString())
        val newMessages = prompt.messages + listOf(aiResponseMessage)
        newMessages.addToMemory(scope, prompt.configuration.messagePolicy.addMessagesToConversation)
        buffer.clear()
      }
      .collect { emit(it) }
  }

@AiDsl
suspend fun Chat.promptMessage(prompt: Prompt, scope: Conversation = Conversation()): String =
  promptMessages(prompt, scope).firstOrNull() ?: throw AIError.NoResponse()

@AiDsl
suspend fun Chat.promptMessages(
  prompt: Prompt,
  scope: Conversation = Conversation()
): List<String> = promptResponse(prompt, scope) { it.message.content }.first

@AiDsl
suspend fun Chat.promptMessageAndUsage(
  prompt: Prompt,
  scope: Conversation = Conversation()
): MessageWithUsage {
  val response = promptMessagesAndUsage(prompt, scope)
  val message = response.messages.firstOrNull() ?: throw AIError.NoResponse()
  return MessageWithUsage(message, response.usage)
}

@AiDsl
suspend fun Chat.promptMessagesAndUsage(
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
    val adaptedPrompt = prompt.adaptPromptToConversationAndModel(scope)

    adaptedPrompt.addMetrics(scope)

    val request =
      CreateChatCompletionRequest(
        user = adaptedPrompt.configuration.user,
        messages = adaptedPrompt.messages,
        n = adaptedPrompt.configuration.numberOfPredictions,
        temperature = adaptedPrompt.configuration.temperature,
        maxTokens = adaptedPrompt.configuration.maxTokens,
        model = prompt.model,
        seed = adaptedPrompt.configuration.seed,
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
