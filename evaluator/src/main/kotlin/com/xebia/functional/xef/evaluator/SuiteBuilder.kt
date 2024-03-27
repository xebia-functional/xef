package com.xebia.functional.xef.evaluator

import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.evaluator.models.ItemResult
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.evaluator.models.OutputResult
import com.xebia.functional.xef.evaluator.models.SuiteResults
import com.xebia.functional.xef.evaluator.output.Html
import java.io.File
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class SuiteBuilder(
  private val description: String,
  private val model: CreateChatCompletionRequestModel
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
  val model: CreateChatCompletionRequestModel
) {

  suspend inline fun <reified E> evaluate(success: List<E>): SuiteResults<E> where
  E : AI.PromptClassifier,
  E : Enum<E> {
    val items =
      items.map { item ->
        println("Evaluating item: ${item.input}")
        val outputResults =
          item.outputs.map { output ->
            val classification =
              AI.classify<E>(item.input, item.context, output.value, model = model)
            println(" |_ ${output.description.value} = classification $classification")
            OutputResult(
              output.description.value,
              item.context,
              output.value,
              classification,
              success.contains(classification)
            )
          }
        ItemResult(item.input, outputResults)
      }
    val suiteResults = SuiteResults(description, model.value, E::class.simpleName, items)
    return suiteResults
  }

  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      description: String,
      model: CreateChatCompletionRequestModel,
      block: suspend SuiteBuilder.() -> Unit
    ): SuiteSpec = SuiteBuilder(description, model).apply { block() }.build()

    inline fun <reified E> toHtml(
      result: SuiteResults<E>,
      htmlFilename: String = "index.html"
    ) where E : AI.PromptClassifier, E : Enum<E> {
      val content = Json.encodeToString(SuiteResults.serializer(serializer<E>()), result)
      // Copy file inside build folder
      val outputPath = System.getProperty("user.dir") + "/build/testSuite"
      File(outputPath).mkdirs()
      val htmlFile = File("$outputPath/$htmlFilename")
      htmlFile.writeText(Html.get(content))
      println("Test suite exported to ${htmlFile.absoluteFile}")
    }
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
