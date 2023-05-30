package com.xebia.functional.xef

import arrow.core.NonEmptyList
import com.xebia.functional.xef.auto.AI
import com.xebia.functional.xef.auto.AIScope
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.prompt
import com.xebia.functional.xef.llm.openai.History
import com.xebia.functional.xef.llm.openai.Message
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.JsonElement

sealed interface AIError {
  val reason: String

  object NoResponse : AIError {
    override val reason: String
      get() = "No response from the AI"
  }

  sealed interface AIClient : AIError {
    data class FailedParsing(val json: JsonElement, val cause: IllegalArgumentException) : AIClient {
      override val reason: String
        get() = "AIClient failed to parse response. Received $json, cause: ${cause.stackTraceToString()}"
    }

    data class NoResponse(val history: NonEmptyList<History<HttpResponse>>) {

    }
  }

//  data class AIClient(val error: Throwable) : AIError {
//    override val reason: String
//      get() = TODO("Not yet implemented")
//  }


  data class MessagesExceedMaxTokenLength(
    val messages: List<Message>,
    val promptTokens: Int,
    val maxTokens: Int
  ) : AIError {
    override val reason: String =
      "Prompt exceeds max token length: $promptTokens + $maxTokens = ${promptTokens + maxTokens}"
  }

  data class PromptExceedsMaxTokenLength(
    val prompt: String,
    val promptTokens: Int,
    val maxTokens: Int
  ) : AIError {
    override val reason: String =
      "Prompt exceeds max token length: $promptTokens + $maxTokens = ${promptTokens + maxTokens}"
  }

  data class JsonParsing(val result: String, val maxAttempts: Int, val cause: Throwable) : AIError {
    override val reason: String
      get() = "Failed to parse the JSON response after $maxAttempts attempts: $result"
  }

  sealed interface Env : AIError {
    data class OpenAI(val errors: NonEmptyList<String>) : Env {
      override val reason: String
        get() = "OpenAI Environment not found: ${errors.all.joinToString("\n")}"
    }

    data class HuggingFace(val errors: NonEmptyList<String>) : Env {
      override val reason: String
        get() = "HuggingFace Environment not found: ${errors.all.joinToString("\n")}"
    }
  }
}
