package com.xebia.functional.xef.auto.gpt4all

import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.gpt4all.Gpt4AllModel
import com.xebia.functional.gpt4all.getOrThrow
import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.auto.ai
import java.nio.file.Path

suspend fun main() {
  val userDir = System.getProperty("user.dir")
  val path = "$userDir/models/gpt4all/ggml-replit-code-v1-3b.bin"

  val supportedModels = Gpt4AllModel.supportedModels()
  supportedModels.forEach {
    println("🤖 ${it.name} ${it.url?.let { "- $it" }}")
  }

  val url = "https://huggingface.co/nomic-ai/ggml-replit-code-v1-3b/resolve/main/ggml-replit-code-v1-3b.bin"
  val modelPath: Path = Path.of(path)
  val GPT4All = GPT4All(url, modelPath)

  println("🤖 GPT4All loaded: $GPT4All")
  /**
   * Uses internally [HuggingFaceLocalEmbeddings] default of "sentence-transformers", "msmarco-distilbert-dot-v5"
   * to provide embeddings for docs in contextScope.
   */

  ai {
    println("🤖 Context loaded: $context")
    GPT4All.use { gpT4All: GPT4All ->
      println("🤖 Generating prompt for context")
      while (true) {
        println("🤖 Enter your prompt: ")
        val userInput = readlnOrNull() ?: break
        gpT4All.promptMessage(
          userInput,
          promptConfiguration = PromptConfiguration {
            docsInContext(2)
            streamToStandardOut(true)
          })
      }
    }
  }.getOrThrow()
}


