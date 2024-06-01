package com.xebia.functional.xef.evaluator

import ai.xef.OpenAI.Chat
import com.xebia.functional.xef.ClassifierAI
import com.xebia.functional.xef.evaluator.models.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class SuiteBuilder(
  private val description: String,
  private val model: Chat
) {

  private val items = mutableListOf<ItemSpec>()

  operator fun ItemSpec.unaryPlus() {
    items.add(this)
  }

  fun build() = SuiteSpec(description, items, model = model)
}

@Serializable
data class SuiteSpec(
  val description: String,
  val items: List<ItemSpec>,
  val model: Chat
) {

  suspend inline fun <reified E> evaluate(
    success: List<E>
  ): SuiteResults<E> where
  E : ClassifierAI.PromptClassifier,
  E : Enum<E> {
    val items =
      items.map { item ->
        println("Evaluating item: ${item.input}")
        val outputResults =
          item.outputs.map { output ->
            val classification =
              model.classify(item.input, item.context, output.value)
            println(" |_ ${output.description.value} = classification $classification")
            OutputResult(
              output.description.value,
              item.context,
              output.value,
              output.tokens,
              classification,
              success.contains(classification)
            )
          }
        ItemResult(item.input, outputResults)
      }
    val suiteResults = SuiteResults(description, model.modelName, E::class.simpleName, items)
    return suiteResults
  }

  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      description: String,
      model: Chat,
      block: suspend SuiteBuilder.() -> Unit
    ): SuiteSpec = SuiteBuilder(description, model).apply { block() }.build()

    inline fun <reified E> toHtml(result: SuiteResults<E>, suiteName: String): Html where
    E : ClassifierAI.PromptClassifier,
    E : Enum<E> =
      Html.get(Json.encodeToString(SuiteResults.serializer(serializer<E>()), result), suiteName)

    inline fun <reified E> toMarkdown(result: SuiteResults<E>, suiteName: String): Markdown where
    E : ClassifierAI.PromptClassifier,
    E : Enum<E> = Markdown.get(result, suiteName)
  }
}

@Serializable
data class ItemSpec(
  val input: String,
  val context: String,
  @SerialName("actual_outputs") val outputs: List<OutputResponse>
) {
  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      input: String,
      context: String,
      block: suspend TestItemBuilder.() -> Unit
    ): ItemSpec = TestItemBuilder(input, context).apply { block() }.build()
  }
}
