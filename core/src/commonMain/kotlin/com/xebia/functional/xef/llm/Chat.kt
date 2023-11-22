package com.xebia.functional.xef.llm

import com.xebia.functional.openai.apis.ChatApi
import com.xebia.functional.openai.models.CreateChatCompletionRequest
import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.openai.models.ext.chat.stream.createChatCompletionStream
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import kotlinx.coroutines.flow.*

@AiDsl
fun ChatApi.promptStreaming(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation
): Flow<String> = flow {
  val messagesForRequestPrompt = PromptCalculator.adaptPromptToConversationAndModel(prompt, scope)

  val request =
    CreateChatCompletionRequest(
      stream = true,
      user = prompt.configuration.user,
      messages = messagesForRequestPrompt.messages,
      n = prompt.configuration.numberOfPredictions,
      temperature = prompt.configuration.temperature,
      maxTokens = prompt.configuration.minResponseTokens,
      model = prompt.model
    )

  this@promptStreaming.createChatCompletionStream(baseUrl, request)
    .mapNotNull { it.choices.mapNotNull { it.delta.content }.reduceOrNull(String::plus) }
    .onEach { emit(it) }
    .fold("", String::plus)
    .also { finalText ->
      val aiResponseMessage = assistant(finalText)
      val newMessages = prompt.messages + listOf(aiResponseMessage)
      newMessages.addToMemory(scope, prompt.configuration.messagePolicy.addMessagesToConversation)
    }
}

@AiDsl
suspend fun ChatApi.promptMessage(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation
): String = promptMessages(prompt, scope).firstOrNull() ?: throw AIError.NoResponse()

@AiDsl
suspend fun ChatApi.promptMessages(
  prompt: Prompt<CreateChatCompletionRequestModel>,
  scope: Conversation
): List<String> =
  scope.metric.promptSpan(prompt) {
    val promptMemories = prompt.messages.toMemory(scope)
    val adaptedPrompt = PromptCalculator.adaptPromptToConversationAndModel(prompt, scope)

    adaptedPrompt.addMetrics(scope)

    val request =
      CreateChatCompletionRequest(
        user = adaptedPrompt.configuration.user,
        messages = adaptedPrompt.messages,
        n = adaptedPrompt.configuration.numberOfPredictions,
        temperature = adaptedPrompt.configuration.temperature,
        maxTokens = adaptedPrompt.configuration.minResponseTokens,
        model = prompt.model
      )

    createChatCompletion(request)
      .body()
      .addMetrics(scope)
      .choices
      .addChoiceToMemory(
        scope,
        promptMemories,
        prompt.configuration.messagePolicy.addMessagesToConversation
      )
      .mapNotNull { it.message.content }
  }
