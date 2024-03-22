package com.xebia.functional.xef.evaluator

import com.xebia.functional.openai.models.CreateChatCompletionRequestModel
import com.xebia.functional.xef.AI
import com.xebia.functional.xef.evaluator.errors.FileNotFound
import com.xebia.functional.xef.evaluator.models.ItemResult
import com.xebia.functional.xef.evaluator.models.OutputResponse
import com.xebia.functional.xef.evaluator.models.OutputResult
import com.xebia.functional.xef.evaluator.models.SuiteResults
import java.io.File
import kotlin.jvm.JvmSynthetic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

  suspend inline fun <reified E> evaluate(): SuiteResults<E> where
  E : AI.PromptClassifier,
  E : Enum<E> {
    val items =
      items.map { item ->
        val outputResults =
          item.outputs.map { output ->
            val classification =
              AI.classify<E>(item.input, item.context, output.value, model = model)
            OutputResult(output.description.value, item.context, output.value, classification)
          }
        ItemResult(item.input, outputResults)
      }
    val suiteResults = SuiteResults(description, model.value, E::class.simpleName, items)
    export(Json.encodeToString(suiteResults))
    return suiteResults
  }

  companion object {
    @JvmSynthetic
    suspend operator fun invoke(
      description: String,
      model: CreateChatCompletionRequestModel,
      block: suspend SuiteBuilder.() -> Unit
    ): SuiteSpec = SuiteBuilder(description, model).apply { block() }.build()
  }

  fun export(content: String): Boolean {
    return arrow.core.raise.recover({
      // Read the content of `index.html` inside resources folder
      val indexHTML =
        SuiteSpec::class.java.getResource("/web/index.html")?.readText()
          ?: raise(FileNotFound("index.html"))
      val scriptJS =
        SuiteSpec::class.java.getResource("/web/script.js")?.readText()
          ?: raise(FileNotFound("script.js"))
      val styleCSS =
        SuiteSpec::class.java.getResource("/web/style.css")?.readText()
          ?: raise(FileNotFound("style.css"))
      val contentJS = "const testData = $content;"

      // Copy all the files inside build folder
      val outputPath = System.getProperty("user.dir") + "/build/testSuite"
      File(outputPath).mkdirs()
      File("$outputPath/index.html").writeText(indexHTML)
      File("$outputPath/script.js").writeText(scriptJS)
      File("$outputPath/style.css").writeText(styleCSS)
      File("$outputPath/content.js").writeText(contentJS)
      val url = File("$outputPath/index.html").toURI()
      println("Test suite exported to $url")
      true
    }) {
      when (it) {
        else -> {
          println(it.message("File not found"))
          false
        }
      }
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
      block: suspend TestItemBuilder.() -> Unit
    ): ItemSpec = TestItemBuilder(input).apply { block() }.build()
  }
}
