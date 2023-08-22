@file:OptIn(BetaOpenAI::class)

package com.xebia.functional.xef.conversation.llm.openai

import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.image.ImageURL
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.tracing.Event
import com.xebia.functional.xef.tracing.Tracker
import kotlin.jvm.JvmInline

val OpenAI.Companion.log: Tracker
  get() = Tracker<OpenAiEvent> {
    when (this) {
      is OpenAiEvent.Chat.Chunk -> value.toString()
      is OpenAiEvent.Chat.Request -> "$value, tokensFromMessages : $tokensFromMessages"
      is OpenAiEvent.Chat.Response -> value.toString()
      is OpenAiEvent.Chat.WithFunctionRequest -> {
          """
        function : ${value.functions}, 
        functionCall : ${value.functionCall},
        messages : ${value.messages} 
        model : ${value.model} 
        maxTokens : ${value.maxTokens} 
        temperature : ${value.temperature} 
        topP : ${value.topP} 
        n : ${value.n} 
        stop : ${value.stop} 
        presencePenalty : ${value.presencePenalty} 
        frequencyPenalty : ${value.frequencyPenalty}
        tokensFromMessages : $tokensFromMessages
        """.trimIndent()
      }
      is OpenAiEvent.Chat.WithFunctionResponse -> value.toString()
      is OpenAiEvent.Completion.Request -> "$value, tokensFromMessages : $tokensFromMessages"
      is OpenAiEvent.Completion.Response -> value.toString()
      is OpenAiEvent.Image.Request -> value.toString()
      is OpenAiEvent.Image.Response -> value.toString()
      is OpenAiEvent.Embedding.Request -> value.toString()
      is OpenAiEvent.Embedding.Response -> value.toString()
    }.let {
      println("OPEN_AI_LOG -> $it")
    }
  }

sealed interface OpenAiEvent : Event {
  sealed interface Embedding : OpenAiEvent {
    data class Request(val value: com.aallam.openai.api.embedding.EmbeddingRequest) : Embedding
    data class Response(val value: com.aallam.openai.api.embedding.EmbeddingResponse) : Embedding
  }

  sealed interface Completion : OpenAiEvent {
    data class Request(val value: com.aallam.openai.api.completion.CompletionRequest, val tokensFromMessages: Int) : Completion
    data class Response(val value: com.aallam.openai.api.completion.TextCompletion) : Completion
  }

  sealed interface Chat : OpenAiEvent {
    data class Request(val value: com.aallam.openai.api.chat.ChatCompletionRequest, val tokensFromMessages: Int) : Chat
    data class Response(val value: com.aallam.openai.api.chat.ChatCompletion) : Chat
    data class Chunk(val value: com.aallam.openai.api.chat.ChatCompletionChunk) : Chat

    data class WithFunctionRequest(val value: com.aallam.openai.api.chat.ChatCompletionRequest, val tokensFromMessages: Int) : Chat
    data class WithFunctionResponse(val value: com.aallam.openai.api.chat.ChatCompletion) : Chat
  }

  sealed interface Image : OpenAiEvent {
    @JvmInline value class Request(val value: com.aallam.openai.api.image.ImageCreation) : Image
    @JvmInline value class Response(val value: List<ImageURL>) : Image
  }
}

fun Conversation.OpenAI(token: String? = null): OpenAI =
  OpenAI(token = token, dispatcher = dispatcher)