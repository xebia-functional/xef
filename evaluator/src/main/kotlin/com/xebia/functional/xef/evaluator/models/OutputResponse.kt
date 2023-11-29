package com.xebia.functional.xef.evaluator.models

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.xebia.functional.xef.evaluator.models.errors.EmptyOutputResponse

@JvmInline
value class OutputResponse(val value: String) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(block: suspend () -> String)
    : Either<EmptyOutputResponse, OutputResponse> =
      either {
        ensure(block().isNotBlank()) { EmptyOutputResponse }
        OutputResponse(block())
      }
  }
}
