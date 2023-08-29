package com.xebia.functional.xef.conversation.gpc

import arrow.core.nonEmptyListOf
import com.xebia.functional.xef.AIError
import com.xebia.functional.xef.conversation.autoClose
import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.env.getenv
import com.xebia.functional.xef.gcp.GcpChat
import com.xebia.functional.xef.gcp.GcpEmbeddings
import com.xebia.functional.xef.llm.models.embeddings.RequestConfig
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  OpenAI.conversation {
    val token = getenv("GCP_TOKEN")
      ?: throw AIError.Env.GCP(nonEmptyListOf("missing GCP_TOKEN env var"))

    val gcp = GcpChat("us-central1-aiplatform.googleapis.com", "xefdemo", "codechat-bison@001", token)
      .let(::autoClose)
    val gcpEmbeddingModel = GcpChat("us-central1-aiplatform.googleapis.com", "xefdemo", "textembedding-gecko", token)
      .let(::autoClose)

    val embeddingResult = GcpEmbeddings(gcpEmbeddingModel)
      .embedQuery("strawberry donuts", RequestConfig(RequestConfig.Companion.User("user")))
    println(embeddingResult)

    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      val answer = gcp.promptMessage(Prompt(userInput))
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
