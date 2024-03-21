package com.xebia.functional.xef.evaluator.models

import arrow.core.EitherNel
import com.xebia.functional.xef.evaluator.SuiteSpecBuilder
import com.xebia.functional.xef.evaluator.models.errors.ValidationError
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuiteSpec(
  val description: String,
  val metric: Metric,
  @SerialName("outputs_description") val outputsDescription: List<String>,
  @SerialName("minimum_score") val minimumScore: Double,
  val items: List<ItemSpec>
) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      description: String,
      metric: Metric = Metric.FactualConsistency,
      block: suspend SuiteSpecBuilder.() -> Unit
    ): EitherNel<ValidationError, SuiteSpec> =
      SuiteSpecBuilder(description, metric).apply { block() }.build()
  }
}
