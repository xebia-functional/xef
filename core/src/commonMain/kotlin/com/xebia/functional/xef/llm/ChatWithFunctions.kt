package com.xebia.functional.xef.llm

import arrow.core.nonFatalOrThrow
import arrow.core.raise.catch
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.auto.AiDsl
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.llm.models.chat.ChatCompletionRequestWithFunctions
import com.xebia.functional.xef.llm.models.chat.ChatCompletionResponseWithFunctions
import com.xebia.functional.xef.llm.models.functions.CFunction
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.vectorstores.ConversationId
import com.xebia.functional.xef.vectorstores.VectorStore
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.uuid.Serializer

interface ChatWithFunctions : Chat {

  suspend fun createChatCompletionWithFunctions(
    request: ChatCompletionRequestWithFunctions
  ): ChatCompletionResponseWithFunctions

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    context: VectorStore,
    serializer: KSerializer<A>,
    conversationId: ConversationId? = null,
    functions: List<CFunction> = emptyList(),
    promptConfiguration: PromptConfiguration = PromptConfiguration.DEFAULTS,
  ): A {
    return prompt(prompt, context, conversationId, functions, { json ->
      Json.decodeFromString(serializer, json)
    }, promptConfiguration)
  }

  @AiDsl
  suspend fun <A> prompt(
    prompt: String,
    context: VectorStore,
    conversationId: ConversationId? = null,
    functions: List<CFunction> = emptyList(),
    serializer: (json: String) -> A,
    promptConfiguration: PromptConfiguration,
  ): A {
    return tryDeserialize(serializer, promptConfiguration.maxDeserializationAttempts) {
      promptMessages(
        prompt = Prompt(prompt),
        context = context,
        conversationId = conversationId,
        functions = functions,
        promptConfiguration
      )
    }
  }

  @AiDsl
  suspend fun <A> prompt(
    prompt: Prompt,
    context: VectorStore,
    conversationId: ConversationId? = null,
    functions: List<CFunction> = emptyList(),
    serializer: (json: String) -> A,
    promptConfiguration: PromptConfiguration,
  ): A {
    return tryDeserialize(serializer, promptConfiguration.maxDeserializationAttempts) {
      promptMessages(
        prompt = prompt,
        context = context,
        conversationId = conversationId,
        functions = functions,
        promptConfiguration
      )
    }
  }

  private suspend fun <A> tryDeserialize(
    serializer: (json: String) -> A,
    maxDeserializationAttempts: Int,
    agent: suspend () -> List<String>
  ): A {
    (0 until maxDeserializationAttempts).forEach { currentAttempts ->
      val result = agent().firstOrNull() ?: throw AIError.NoResponse()
      catch({
        return@tryDeserialize serializer(result)
      }) { e: Throwable ->
        if (currentAttempts == maxDeserializationAttempts)
          throw AIError.JsonParsing(result, maxDeserializationAttempts, e.nonFatalOrThrow())
        // TODO else log attempt ?
      }
    }
    throw AIError.NoResponse()
  }
}
