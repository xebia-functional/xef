package com.xebia.functional

import arrow.core.NonEmptyList

sealed interface AIError {
  val reason: String

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
    data class HuggingFace(val errors: NonEmptyList<String>): Env {
      override val reason: String
        get() = "HuggingFace Environment not found: ${errors.all.joinToString("\n")}"
    }
  }

  sealed interface Chain : AIError {
    override val reason: String

    data class InvalidInputs(override val reason: String) : Chain

    interface Sequence : Chain {
      data class InvalidOutputs(override val reason: String): Sequence
      data class InvalidKeys(override val reason: String): Sequence
    }

    data class InvalidTemplate(override val reason: String) : Chain
  }
}