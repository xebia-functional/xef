package com.xebia.functional.xef

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import com.xebia.functional.xef.llm.models.chat.Message
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

sealed class AIError @JvmOverloads constructor(message: String, cause: Throwable? = null) :
  RuntimeException(message, cause) {

  class NoResponse : AIError("No response from the AI")

  data class MessagesExceedMaxTokenLength(
    val messages: List<Message>,
    val promptTokens: Int,
    val maxTokens: Int
  ) :
    AIError(
      "Prompt exceeds max token length: $promptTokens + $maxTokens = ${promptTokens + maxTokens}"
    )

  data class PromptExceedsMaxTokenLength(
    val prompt: String,
    val promptTokens: Int,
    val maxTokens: Int
  ) :
    AIError(
      "Prompt exceeds max token length: $promptTokens + $maxTokens = ${promptTokens + maxTokens}"
    )

  data class JsonParsing(val result: String, val maxAttempts: Int, override val cause: Throwable) :
    AIError("Failed to parse the JSON response after $maxAttempts attempts: $result", cause)

  sealed class Env @JvmOverloads constructor(message: String, cause: Throwable? = null) :
    AIError(message, cause) {
    data class AuthInfoMissing(val errors: NonEmptyList<String>) :
      Env("OpenAI Environment not found: ${errors.all.joinToString("\n")}") {
        companion object {

          @JvmStatic
          operator fun invoke(error: String): AuthInfoMissing =
            AuthInfoMissing(nonEmptyListOf(error))
        }
      }
  }
}
