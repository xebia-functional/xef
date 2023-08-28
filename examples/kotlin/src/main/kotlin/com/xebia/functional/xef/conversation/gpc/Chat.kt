package com.xebia.functional.xef.conversation.gpc

import com.xebia.functional.xef.conversation.llm.openai.OpenAI
import com.xebia.functional.xef.gcp.GcpChat
import com.xebia.functional.xef.prompt.Prompt

suspend fun main() {
  OpenAI.conversation {
    val gcp =
      autoClose(
        GcpChat("us-central1-aiplatform.googleapis.com", "xef-demo", "codechat-bison@001", "token")
      )
    val gcpEmbedding = autoClose(GcpChat("us-central-aiplatform.googleapis.com", "xef-demo", "textembedding-gecko", "token"))

    while (true) {
      print("\n🤖 Enter your question: ")
      val userInput = readlnOrNull() ?: break
      val answer = gcp.promptMessage(Prompt(userInput))
      println("\n🤖 $answer")
    }
    println("\n🤖 Done")
  }
}
