package com.xebia.functional.xef.auto.gpt4all

import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.gpt4all.Gpt4AllModel
import com.xebia.functional.gpt4all.conversation
import com.xebia.functional.xef.auto.PromptConfiguration
import java.nio.file.Path
import kotlinx.coroutines.flow.onCompletion

suspend fun main() {
  val userDir = System.getProperty("user.dir")
  val path = "$userDir/models/gpt4all/ggml-replit-code-v1-3b.bin"

  Gpt4AllModel.supportedModels.forEach { println("🤖 ${it.name} ${it.url?.let { "- $it" }}") }

  val url =
    "https://huggingface.co/nomic-ai/ggml-replit-code-v1-3b/resolve/main/ggml-replit-code-v1-3b.bin"
  val modelPath: Path = Path.of(path)
  val model = GPT4All(url, modelPath)

  println("🤖 GPT4All loaded: $GPT4All")
  /**
   * Uses internally [HuggingFaceLocalEmbeddings] default of "sentence-transformers",
   * "msmarco-distilbert-dot-v5" to provide embeddings for docs in contextScope.
   */
  GPT4All.conversation {
    println("🤖 Context loaded: $store")
    // hack until https://github.com/nomic-ai/gpt4all/pull/1126 is accepted or merged
    val out = System.out
    model.use { gpT4All: GPT4All ->
      while (true) {
        print("\n🤖 Enter your question: ")
        val userInput = readlnOrNull() ?: break
        gpT4All
          .promptStreaming(
            userInput,
            this,
            promptConfiguration = PromptConfiguration { docsInContext(2) }
          )
          .onCompletion { println("\n🤖 Done") }
          .collect { out.print(it) }
      }
    }
  }
}
