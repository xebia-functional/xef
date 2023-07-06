package com.xebia.functional.xef.auto.google

import com.xebia.functional.xef.google.GoogleEmbeddings
import com.xebia.functional.xef.google.GoogleModel
import com.xebia.functional.xef.vectorstores.LocalVectorStore

suspend fun main() {

  val project = "xefdemo"
  val location = "us-central1"
  val model = "text-bison"
  val embeddingsModel = "textembedding-gecko"
  val context = LocalVectorStore(GoogleEmbeddings(project, location, embeddingsModel))

  val googleAI = GoogleModel(project, location, model)

  println("🤖 Vertex.ai loaded: $googleAI")

  googleAI.use {
    println("🤖 Context loaded: $context")
    println("🤖 Generating prompt for context")
    while (true) {
      println("🤖 Enter your prompt: ")
      val userInput = readlnOrNull() ?: break
      val response = googleAI.promptMessage(userInput, context)
      println("🤖 Response: $response")
    }
  }
}


// projects/xefdemo/locations/us-central1/endpoints/textembedding-gecko
// projects/xefdemo/locations/us-central1/publishers/google/models/textembedding-gecko:predict
