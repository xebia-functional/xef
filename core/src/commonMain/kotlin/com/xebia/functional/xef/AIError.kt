package com.xebia.functional.xef

import arrow.core.NonEmptyList

sealed interface AIError {
  val reason: String

  object NoResponse : AIError {
    override val reason: String
      get() = "No response from the AI"
  }

  data class JsonParsing(
    val result: String,
    val maxAttempts: Int,
    val cause: IllegalArgumentException
  ) : AIError {
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
