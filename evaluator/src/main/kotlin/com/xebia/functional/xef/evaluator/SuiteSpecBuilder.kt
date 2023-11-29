package com.xebia.functional.xef.evaluator

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.recover
import arrow.core.raise.either
import arrow.core.raise.zipOrAccumulate
import arrow.core.raise.RaiseAccumulate
import arrow.core.raise.ensure
import com.xebia.functional.xef.evaluator.models.Metric
import com.xebia.functional.xef.evaluator.models.OutputDescription
import com.xebia.functional.xef.evaluator.models.ItemSpec
import com.xebia.functional.xef.evaluator.models.SuiteSpec
import com.xebia.functional.xef.evaluator.models.errors.*

class SuiteSpecBuilder(private val description: String, private val metric: Metric) {
  private var minimumScore: Double = 0.7

  private val outputsDescription = mutableListOf<Either<ValidationError, OutputDescription>>()
  private val itemsSpec = mutableListOf<EitherNel<ValidationError, ItemSpec>>()

  suspend fun outputDescription(block: suspend () -> String) =
    outputsDescription.add(OutputDescription.invoke { block() })

  suspend fun itemSpec(input: String, block: suspend ItemSpecBuilder.() -> Unit) =
    itemsSpec.add(ItemSpecBuilder(input).apply { block() }.build())

  fun build(): EitherNel<ValidationError, SuiteSpec> = either {
    zipOrAccumulate(
      descriptionValidator(),
      outputsDescriptionValidator(),
      itemsValidator()
    ) { _, outputsDescription, items -> SuiteSpec(description, metric, outputsDescription, minimumScore, items) }
  }

  private fun descriptionValidator(): RaiseAccumulate<ValidationError>.() -> Unit =
    { ensure(description.isNotBlank()) { EmptySuiteSpecDescription } }

  private fun outputsDescriptionValidator(): RaiseAccumulate<ValidationError>.() -> List<String> =
    {
      when {
        outputsDescription.isEmpty() -> raise(OutputsDescriptionNotProvided)
        outputsDescription.size > itemsSpec.size -> raise(MoreOutputsDescriptionThanItemsSpec)
        outputsDescription.size < itemsSpec.size -> raise(LessOutputsDescriptionThanItemsSpec)
        else -> mapOrAccumulate(outputsDescription.withIndex()) {
          it.value.recover { _ -> raise(EmptySuiteSpecOutputDescription(it.index)) }.bind().value
        }
      }
    }

  private fun itemsValidator(): RaiseAccumulate<ValidationError>.() -> List<ItemSpec> =
    {
      mapOrAccumulate(itemsSpec.withIndex()) {
        val itemSpec = it.value.recover { _ -> raise(EmptyItemSpecOutputResponse(it.index)) }.bind()

        if (outputsDescription.size == itemsSpec.size) {
          ensure(itemSpec.outputs.size == outputsDescription.size) {
            InvalidNumberOfItemSpecOutputResponse(it.index)
          }
        }

        itemSpec
      }
    }
}
