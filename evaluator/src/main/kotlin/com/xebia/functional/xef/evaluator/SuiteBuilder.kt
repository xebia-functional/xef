package com.xebia.functional.xef.evaluator

import com.xebia.functional.xef.evaluator.models.OutputDescription
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SuiteBuilder(private val description: String, private val metric: String) {

  private val outputsDescription: MutableList<String> = mutableListOf()

  private var minimumScore: Double = 0.7

  private val items = mutableListOf<TestSpecItem>()

  operator fun TestSpecItem.unaryPlus() {
    items.add(this)
  }

  operator fun OutputDescription.unaryPlus() {
    outputsDescription.add(this.value)
  }

  fun build() = TestsSpec(description, metric, outputsDescription, minimumScore, items)
}

@Serializable
data class TestsSpec(
  val description: String,
  val metric: String,
  @SerialName("outputs_description") val outputsDescription: List<String>,
  @SerialName("minimum_score") val minimumScore: Double,
  val items: List<TestSpecItem>
) {

  fun toJSON(): String = Json.encodeToString(this)

  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      description: String,
      metric: String = "FactualConsistencyMetric",
      block: suspend SuiteBuilder.() -> Unit
    ): TestsSpec = SuiteBuilder(description, metric).apply { block() }.build()
  }
}

@Serializable
data class TestSpecItem(
  val input: String,
  val context: List<String>,
  @SerialName("actual_outputs") val outputs: List<String>
) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      input: String,
      block: suspend TestItemBuilder.() -> Unit
    ): TestSpecItem = TestItemBuilder(input).apply { block() }.build()
  }
}
