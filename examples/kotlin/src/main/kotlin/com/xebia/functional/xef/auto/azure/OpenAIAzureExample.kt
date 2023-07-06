package com.xebia.functional.xef.auto.azure

import com.xebia.functional.xef.auto.PromptConfiguration
import com.xebia.functional.xef.auto.llm.openai.OpenAI
import com.xebia.functional.xef.auto.llm.openai.OpenAIEmbeddings
import com.xebia.functional.xef.pdf.pdf
import com.xebia.functional.xef.vectorstores.LocalVectorStore


suspend fun main() {

  // Create an instance of GPT4All with the azure location
  val azure = OpenAI.azure(
    resourceName = "xefdemo",
    deploymentId = "textembedding-gecko",
    apiVersion = "v1"
  )

  val chatModel = azure.GPT_3_5_TURBO

  // Create an instance of the OPENAI embeddings
  val embeddings = azure.TEXT_EMBEDDING_ADA_002

  // Create a LocalVectorStore and initialize it with OpenAI Embeddings
  val vectorStore = LocalVectorStore(OpenAIEmbeddings(embeddings))

  // Fetch and add texts from a PDF document to the vector store
  val results = pdf("https://arxiv.org/pdf/2305.10601.pdf")
  vectorStore.addTexts(results)

  // Prompt the GPT4All model with a question and provide the vector store for context
  val result: List<String> =
    chatModel.promptMessage(
      question = "What is the Tree of Thoughts framework about?",
      context = vectorStore,
      promptConfiguration = PromptConfiguration {
        docsInContext(5)
      }
    )


  // Print the response
  println(result)
}
