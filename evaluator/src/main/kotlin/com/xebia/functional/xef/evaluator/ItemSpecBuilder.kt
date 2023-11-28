package com.xebia.funcional.xef.evaluator

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.recover
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.zipOrAccumulate
import com.xebia.funcional.xef.evaluator.models.ContextDescription
import com.xebia.funcional.xef.evaluator.models.OutputResponse
import com.xebia.funcional.xef.evaluator.models.ItemSpec
import com.xebia.funcional.xef.evaluator.models.errors.EmptyItemSpecInput
import com.xebia.funcional.xef.evaluator.models.errors.ValidationError

class ItemSpecBuilder(val input: String) {
  private val contexts = mutableListOf<Either<ValidationError, ContextDescription>>()
  private val outputs = mutableListOf<Either<ValidationError, OutputResponse>>()

  suspend fun contextDescription(block: suspend () -> String) =
    contexts.add(ContextDescription.invoke { block() })

  suspend fun outputResponse(block: suspend () -> String) =
    outputs.add(OutputResponse.invoke { block() })

  fun build(): EitherNel<ValidationError, ItemSpec> = either {
      zipOrAccumulate(
        { ensure(input.isNotBlank()) { EmptyItemSpecInput } },
        {
          mapOrAccumulate(contexts.withIndex()) { nameAndIndex ->
            nameAndIndex.value.recover { error -> raise(error) }.bind()
          }.map { it.value }
        },
        {
          mapOrAccumulate(outputs.withIndex()) { nameAndIndex ->
            nameAndIndex.value.recover { error -> raise(error) }.bind()
          }.map { it.value }
        }
      ) { _, contexts, outputs -> ItemSpec(input, contexts, outputs) }
    }
}
