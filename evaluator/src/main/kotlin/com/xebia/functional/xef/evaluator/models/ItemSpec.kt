package com.xebia.funcional.xef.evaluator.models

import arrow.core.EitherNel
import com.xebia.funcional.xef.evaluator.ItemSpecBuilder
import com.xebia.funcional.xef.evaluator.models.errors.ValidationError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemSpec(
  val input: String,
  val context: List<String>,
  @SerialName("actual_outputs") val outputs: List<String>
) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      input: String,
      block: suspend ItemSpecBuilder.() -> Unit
    ): EitherNel<ValidationError, ItemSpec> = ItemSpecBuilder(input).apply { block() }.build()
  }
}
