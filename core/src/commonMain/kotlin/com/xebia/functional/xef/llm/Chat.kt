package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

interface Chat : LLM {
  val modelType: ModelType

  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse

  suspend fun createChatCompletions(request: ChatCompletionRequest): Flow<ChatCompletionChunk>

  fun tokensFromMessages(messages: List<Message>): Int

  @AiDsl
  fun promptStreaming(prompt: Prompt, scope: Conversation): Flow<String> = flow {
    val messagesForRequestPrompt =
      PromptCalculator.adaptPromptToConversationAndModel(prompt, scope, this@Chat)

    val request =
      ChatCompletionRequest(
        model = name,
        user = prompt.configuration.user,
        messages = messagesForRequestPrompt.messages,
        n = prompt.configuration.numberOfPredictions,
        temperature = prompt.configuration.temperature,
        maxTokens = prompt.configuration.minResponseTokens,
        streamToStandardOut = true
      )

    val buffer = StringBuilder()
    createChatCompletions(request)
      .onEach {
        it.choices.forEach { choice ->
          val text = choice.delta?.content ?: ""
          buffer.append(text)
        }
      }
      .onCompletion {
        val message = assistant(buffer.toString())
        MemoryManagement.addMemoriesAfterStream(this@Chat, request, scope, listOf(message))
      }
      .collect { emit(it.choices.mapNotNull { it.delta?.content }.joinToString("")) }
  }

  @AiDsl
  suspend fun promptMessage(prompt: Prompt, scope: Conversation): String =
    promptMessages(prompt, scope).firstOrNull() ?: throw AIError.NoResponse()

  @AiDsl
  suspend fun promptMessages(prompt: Prompt, scope: Conversation): List<String> {

    val adaptedPrompt = PromptCalculator.adaptPromptToConversationAndModel(prompt, scope, this@Chat)

    fun chatRequest(): ChatCompletionRequest =
      ChatCompletionRequest(
        model = name,
        user = adaptedPrompt.configuration.user,
        messages = adaptedPrompt.messages,
        n = adaptedPrompt.configuration.numberOfPredictions,
        temperature = adaptedPrompt.configuration.temperature,
        maxTokens = adaptedPrompt.configuration.minResponseTokens,
        functions = listOfNotNull(adaptedPrompt.function),
        functionCall = adaptedPrompt.function?.let { mapOf("name" to (it.name)) }
      )

    return MemoryManagement.run {
      when (this@Chat) {
        is ChatWithFunctions ->
          // we only support functions for now with GPT_3_5_TURBO_FUNCTIONS
          if (modelType == ModelType.GPT_3_5_TURBO_FUNCTIONS) {
            val request = chatRequest()
            createChatCompletionWithFunctions(request)
              .choices
              .addChoiceWithFunctionsToMemory(this@Chat, request, scope)
              .mapNotNull { it.message?.functionCall?.arguments }
          } else {
            val request = chatRequest()
            createChatCompletion(request)
              .choices
              .addChoiceToMemory(this@Chat, request, scope)
              .mapNotNull { it.message?.content }
          }
        else -> {
          val request = chatRequest()
          createChatCompletion(request)
            .choices
            .addChoiceToMemory(this@Chat, request, scope)
            .mapNotNull { it.message?.content }
        }
      }
    }
  }
}
