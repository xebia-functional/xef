package com.xebia.functional.xef.llm

import com.xebia.functional.tokenizer.ModelType
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.AiDsl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.llm.models.chat.*
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.prompt.templates.assistant
import kotlinx.coroutines.flow.*

interface Chat : LLM {
  val modelType: ModelType

  suspend fun createChatCompletion(request: ChatCompletionRequest): ChatCompletionResponse

  suspend fun createChatCompletions(request: ChatCompletionRequest): Flow<ChatCompletionChunk>

  fun tokensFromMessages(messages: List<Message>): Int

  @AiDsl
  fun promptStreaming(prompt: Prompt, scope: Conversation): Flow<String> = flow {
    val request = chatRequest(prompt = prompt, conversation = scope, stream = true)

    createChatCompletions(request)
      .mapNotNull { it.choices.mapNotNull { it.delta?.content }.reduceOrNull(String::plus) }
      .onEach { emit(it) }
      .fold("", String::plus)
      .also { finalText ->
        val message = assistant(finalText)
        MemoryManagement.addMemoriesAfterStream(this@Chat, request, scope, listOf(message))
      }
  }

  @AiDsl
  suspend fun promptMessage(prompt: Prompt, scope: Conversation): String =
    promptMessages(prompt, scope).firstOrNull() ?: throw AIError.NoResponse()

  suspend fun chatRequest(prompt: Prompt, conversation: Conversation, stream: Boolean): ChatCompletionRequest {
    val adaptedPrompt = PromptCalculator.adaptPromptToConversationAndModel(prompt, conversation, this@Chat)
    return ChatCompletionRequest(
      model = name,
      user = adaptedPrompt.configuration.user,
      messages = adaptedPrompt.messages,
      n = adaptedPrompt.configuration.numberOfPredictions,
      temperature = adaptedPrompt.configuration.temperature,
      maxTokens = adaptedPrompt.configuration.minResponseTokens,
      functions = listOfNotNull(adaptedPrompt.function),
      functionCall = adaptedPrompt.function?.let { mapOf("name" to (it.name)) },
      stream = stream
    )
  }

  @AiDsl
  suspend fun promptMessages(prompt: Prompt, scope: Conversation): List<String> {

    return MemoryManagement.run {
      when (this@Chat) {
        is ChatWithFunctions ->
          // we only support functions for now with GPT_3_5_TURBO_FUNCTIONS
          if (modelType == ModelType.GPT_3_5_TURBO_FUNCTIONS) {
            val request = chatRequest(prompt = prompt, conversation = scope, stream = false)
            createChatCompletionWithFunctions(request)
              .choices
              .addChoiceWithFunctionsToMemory(this@Chat, request, scope)
              .mapNotNull { it.message?.functionCall?.arguments }
          } else {
            val request = chatRequest(prompt = prompt, conversation = scope, stream = false)
            createChatCompletion(request)
              .choices
              .addChoiceToMemory(this@Chat, request, scope)
              .mapNotNull { it.message?.content }
          }
        else -> {
          val request = chatRequest(prompt = prompt, conversation = scope, stream = false)
          createChatCompletion(request)
            .choices
            .addChoiceToMemory(this@Chat, request, scope)
            .mapNotNull { it.message?.content }
        }
      }
    }
  }
}
