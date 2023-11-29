package com.xebia.functional.xef.evaluator.models

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.xebia.functional.xef.evaluator.models.errors.EmptyOutputDescription

@JvmInline
value class OutputDescription(val value: String) {
  operator fun unaryPlus() {
    either {
      ensure(value.isNotBlank()) { EmptyOutputDescription }
      OutputDescription(value)
    }
  }

  companion object {
    @JvmSynthetic
    suspend operator fun invoke(block: suspend () -> String): Either<EmptyOutputDescription, OutputDescription> =
      either {
        ensure(block().isNotBlank()) { EmptyOutputDescription }
        OutputDescription(block())
      }
  }
}
