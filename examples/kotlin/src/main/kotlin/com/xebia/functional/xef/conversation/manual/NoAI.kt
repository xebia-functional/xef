package com.xebia.functional.xef.conversation.manual

import com.xebia.functional.gpt4all.GPT4All
import com.xebia.functional.gpt4all.HuggingFaceLocalLLMEmbeddings
import com.xebia.functional.gpt4all.huggingFaceUrl
import com.xebia.functional.xef.conversation.Conversation
import com.xebia.functional.xef.pdf.pdf
import com.xebia.functional.xef.prompt.Prompt
import com.xebia.functional.xef.store.LocalVectorStore
import java.nio.file.Path

suspend fun main() {
  // Choose your base folder for downloaded models
  val userDir = System.getProperty("user.dir")

  // Specify the local model path
  val modelPath: Path = Path.of("$userDir/models/gpt4all/ggml-gpt4all-j-v1.3-groovy.bin")

  // Specify the Hugging Face URL for the model
  val url = huggingFaceUrl("orel12", "ggml-gpt4all-j-v1.3-groovy", "bin")

  // Create an instance of GPT4All with the local model
  val gpt4All = GPT4All(url, modelPath)

  // Create an instance of the embeddings
  val embeddings = HuggingFaceLocalLLMEmbeddings.DEFAULT
  val scope = Conversation(LocalVectorStore(embeddings))

  // Fetch and add texts from a PDF document to the vector store
  val results = pdf("https://arxiv.org/pdf/2305.10601.pdf")
  scope.store.addTexts(results)

  // Prompt the GPT4All model with a question and provide the vector store for context
  val result: String =
    gpt4All.use {
      it.promptMessage(
        prompt = Prompt("What is the Tree of Thoughts framework about?"),
        scope = scope
      )
    }

  // Print the response
  println(result)
}
