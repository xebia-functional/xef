package com.xebia.funcional.xef.evaluator.models

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.xebia.funcional.xef.evaluator.models.errors.EmptyContextDescription

@JvmInline
value class ContextDescription(val value: String) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(block: suspend () -> String): Either<EmptyContextDescription, ContextDescription> =
      either {
        ensure(block().isNotBlank()) { EmptyContextDescription }
        ContextDescription(block())
      }
  }
}
