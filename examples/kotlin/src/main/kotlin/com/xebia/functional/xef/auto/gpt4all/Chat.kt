package com.xebia.functional.xef.auto.gpt4all

import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.gpt4all.LLModel
import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.gpt4all.huggingFaceUrl
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.auto.ai
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.pdf.pdf
import java.nio.file.Path

suspend fun main() {
  val userDir = System.getProperty("user.dir")
  val path = "$userDir/models/gpt4all/ggml-gpt4all-j-v1.3-groovy.bin"
  val url = huggingFaceUrl("orel12", "ggml-gpt4all-j-v1.3-groovy", "bin")
  val modelType = LLModel.Type.GPTJ
  val modelPath: Path = Path.of(path)
  val GPT4All = GPT4All(url, modelPath, modelType)

  println("🤖 GPT4All loaded: $GPT4All")

  val pdfUrl = "https://www.europarl.europa.eu/RegData/etudes/STUD/2023/740063/IPOL_STU(2023)740063_EN.pdf"

  /**
   * Uses internally [HuggingFaceLocalEmbeddings] default of "sentence-transformers", "msmarco-distilbert-dot-v5"
   * to provide embeddings for docs in contextScope.
   */

  ai {
    println("🤖 Loading PDF: $pdfUrl")
    contextScope(pdf(pdfUrl)) {
      println("🤖 Context loaded: $context")
      GPT4All.use { gpT4All: GPT4All ->
        println("🤖 Generating prompt for context")
        val prompt = gpT4All.promptMessage(
          "Describe in one sentence what the context is about.",
          promptConfiguration = PromptConfiguration {
            docsInContext(2)
          })
        println("🤖 Generating images for prompt: \n\n$prompt")
        val images =
          OpenAI.DEFAULT_IMAGES.images(prompt.joinToString("\n"), promptConfiguration = PromptConfiguration {
            docsInContext(1)
          })
        println("🤖 Generated images: \n\n${images.data.joinToString("\n") { it.url }}")
      }
    }
  }.getOrThrow()
}


